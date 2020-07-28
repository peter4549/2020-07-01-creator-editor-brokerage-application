package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.*
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.hashString
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.*
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.squareup.okhttp.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_pr.view.text_view_public_name
import kotlinx.android.synthetic.main.item_view_message.view.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment(private var chatRoom: ChatRoomModel? = null,
                   private val pr: PrModel? = null) : Fragment() {

    private lateinit var collectionReference: CollectionReference
    private lateinit var currentUserId: String
    private lateinit var currentUserPublicName: String
    private lateinit var listenerRegistration: ListenerRegistration
    private val otherUserTokens = mutableListOf<String>()
    private var firstMessage = true
    private var publisherId = ""
    private var publisherName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (chatRoom != null) {
            button_input.isEnabled = true
            MainActivity.currentChatRoomId = chatRoom!!.roomId
            publisherId = chatRoom!!.publisherId
            publisherName = chatRoom!!.publisherName
            collectionReference = FirebaseFirestore.getInstance()
                .collection(COLLECTION_CHAT).document(chatRoom!!.roomId)
                .collection(COLLECTION_CHAT_MESSAGES)

            initRecyclerView()
        } else {
            publisherId = pr!!.publisherId
            publisherName = pr.publisherName
        }

        button_input.setOnClickListener {
            // getGroupNotificationKey() // test!
            if (edit_text_message.text.isNotBlank()) {
                val message = edit_text_message.text.toString()
                if (chatRoom == null)
                    createChatRoom(message)
                else
                    sendMessage(message)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currentUserId = MainActivity.currentUser!!.id
        currentUserPublicName = MainActivity.currentUser!!.publicName
    }

    override fun onStop() {
        removeChatMessageSnapshotListener()
        MainActivity.currentChatRoomId = null
        super.onStop()
    }

    private fun removeChatMessageSnapshotListener() {
        if (::listenerRegistration.isInitialized)
            listenerRegistration.remove()
    }

    private fun initRecyclerView() {
        recycler_view_chat_messages.apply {
            setHasFixedSize(true)
            adapter = ChatMessageRecyclerViewAdapter()
            layoutManager = LayoutManagerWrapper(context, 1)
        }
    }

    private fun createChatRoom(initMessage: String) {
        val creationTime = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val chatRoomId =
            hashString(currentUserId + pr?.publisherId + creationTime).chunked(16)[0]
        val chatMessage = ChatMessageModel()

        chatMessage.senderName = MainActivity.currentUser!!.publicName
        chatMessage.message = initMessage
        chatMessage.time = creationTime
        MainActivity.currentChatRoomId = chatRoomId

        FirebaseFirestore.getInstance()
            .collection(COLLECTION_CHAT)
            .document(chatRoomId)
            .collection(COLLECTION_CHAT_MESSAGES)
            .add(chatMessage)
            .addOnCompleteListener {  task ->
                if (task.isSuccessful) {
                    chatRoom = ChatRoomModel()
                    chatRoom?.creationTime = creationTime
                    chatRoom?.groupNotificationKey = ""
                    chatRoom?.publisherId = publisherId
                    chatRoom?.publisherName = publisherName
                    chatRoom?.roomId = chatRoomId
                    chatRoom?.userIds = mutableListOf(currentUserId, publisherId)
                    chatRoom?.userPublicNames =
                        mutableListOf(currentUserPublicName, publisherName)

                    FirebaseFirestore.getInstance()
                        .collection(COLLECTION_CHAT)
                        .document(chatRoomId).set(chatRoom!!).addOnCompleteListener { setChatRoomTask ->
                            if (setChatRoomTask.isSuccessful) {
                                showToast(requireContext(), getString(R.string.chat_room_creation_success_message))
                                sendCloudMessageAfterGetTokens()
                            }
                        }

                    collectionReference = FirebaseFirestore.getInstance()
                        .collection(COLLECTION_CHAT).document(chatRoomId)
                        .collection(COLLECTION_CHAT_MESSAGES)

                    initRecyclerView()
                } else {
                    showToast(requireContext(), getString(R.string.chat_room_creation_failure_message))
                    println("${PrFragment.TAG}: ${task.exception}")
                }
            }
    }

    private fun sendMessage(message: String) { // 채팅메시지 쏘는 부분. 푸시 메시지도 보내야함.
        val chatMessage = ChatMessageModel()

        chatMessage.senderName = currentUserPublicName
        chatMessage.message = message
        chatMessage.time = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())

        collectionReference.add(chatMessage).addOnCompleteListener {  task ->
            if (task.isSuccessful) {
                if (firstMessage)
                    sendCloudMessageAfterGetTokens()
                else
                    sendCloudMessage()
                println("$TAG: message sent")
            }
            else {
                showToast(requireContext(), getString(R.string.chat_message_sending_failure_message))
                println("$TAG: ${task.exception}")
            }
        }
    }

    // 세 명 이상으로 채팅방이 생성될 때, 또는 멤버 초대시 수행되어야 함.
    private fun getGroupNotificationKey() { // 시발 딱히 필요없어보임;;
        val creationTime = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val url = "https://fcm.googleapis.com/fcm/notification"
        val groupNotification = GroupNotificationModel()

        groupNotification.operation = "create"
        if (chatRoom == null)
            groupNotification.notification_key_name =
                hashString(listOf(currentUserId, publisherId).joinToString() + creationTime).chunked(16)[0]
        else {
            groupNotification.notification_key_name =
                hashString(chatRoom!!.userIds.joinToString() + creationTime).chunked(16)[0]
            groupNotification.registration_ids = chatRoom!!.userPushTokens // 여기에 다이렉트로 메시지 꼽으면 끝나는거같은데 시바ㅏㄹ''
        }

        val requestBody =
            RequestBody.create(MediaType.parse("application/json; charset=utf8"),
                Gson().toJson(groupNotification))

        val request = Request.Builder().header("Content-Type", "application/json")
            .addHeader("Authorization", API_KEY)
            .addHeader("project_id", SENDER_ID)
            .url(url)
            .post(requestBody)
            .build()

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                showToast(requireContext(), "그룹 메시징 키 생성에 실패했습니다.")
                println("$TAG ${e?.message}")
            }

            override fun onResponse(response: Response?) {
                if (response?.isSuccessful == true)
                    println("$TAG::thisisforresultgetTok ${response.body()?.string()}") // 뭐날라오는지 확인. 제이슨으로받을것.
                // 여기서 받아 전역변수에 저장할 것.
                // 순서.. 다수의 채팅방이 생성되는 경우는 없다. 초대되는 경우만 존재함 즉, 이거는 업데이트만이 수행될것.
                else {
                    showToast(requireContext(), "그룹 메시징 키 생성에 실패했습니다.")
                    println("$TAG: Group messaging key generation failed")
                }
            }
        })
    }

    private fun sendCloudMessageAfterGetTokens() {
        // 그룹인지 아닌지 판단해서 보낼 것.
        // 여기서 분화할것. 만약 멤버가 2명 아래 로직.
        // 다수의 유저를 불러올때 로직. .where("UserModel.KEY_ID", "in", ["id1", "id2"]) // 일단 이론임.

        FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS).whereIn(
                UserModel.KEY_ID, chatRoom!!.userIds)  // 멤버들한테 다 보내야함. 이부분은 단체 채팅부분에서 수정할것. 이게 아니라 챗 룸의 멤버아이디..로 서칭. 여기서 단체로 서칭.
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.documents?.let { documentSnapshots ->
                        for (documentSnapshot in
                        documentSnapshots.filter { it[UserModel.KEY_ID] != currentUserId }) {
                            otherUserTokens.add(documentSnapshot?.data?.get(KEY_PUSH_TOKEN) as String)
                        }

                        if (otherUserTokens.count() > 1) {
                            // 다수의 유저 케이스.
                            // 여기서 그토큰얻어오는 로직도 필요함.
                        } else {
                            sendSingleCloudMessage(otherUserTokens[0])
                            println("$TAG: publisher id found")
                        }

                        firstMessage = false
                    } ?: run {
                        showToast(requireContext(), getString(R.string.failed_to_find_publisher))
                        return@addOnCompleteListener
                    }
                } else {
                    showToast(requireContext(), getString(R.string.failed_to_find_publisher))
                    println("$TAG: ${task.exception}")
                }
            }
    }

    private fun sendCloudMessage() {
        if (otherUserTokens.count() > 1) {
            // 그룹 메시지 토큰을 사용하여 보내기.
        } else {
            sendSingleCloudMessage(otherUserTokens[0])
        }
    }

    private fun sendSingleCloudMessage(pushToken: String) { // 이 함수, 통일할 것.
        val url = "https://fcm.googleapis.com/fcm/send"
        val cloudMessage = CloudMessageModel()

        cloudMessage.to = pushToken

        // cloudMessage.notification.click_action = ACTION_MAIN
        cloudMessage.notification.title = currentUserPublicName
        cloudMessage.notification.text = edit_text_message.text.toString()

        cloudMessage.data.message = edit_text_message.text.toString()
        cloudMessage.data.roomId = chatRoom!!.roomId
        cloudMessage.data.senderPublicName = currentUserPublicName

        val requestBody =
            RequestBody.create(MediaType.parse("application/json; charset=utf8"),
            Gson().toJson(cloudMessage))
        val request = Request.Builder().header("Content-Type", "application/json")
            .addHeader("Authorization", API_KEY)
            .url(url)
            .post(requestBody)
            .build()

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                showToast(requireContext(), getString(R.string.chat_message_sending_failure_message))
                println("$TAG: ${e?.message}")
            }

            override fun onResponse(response: Response?) {
                if (response?.isSuccessful == true)
                    println("$TAG: ${response.body()?.string()}")
                else {
                    showToast(requireContext(), getString(R.string.chat_message_sending_failure_message))
                    println("$TAG: message sending failed")
                }
            }
        })
    }

    inner class ChatMessageRecyclerViewAdapter :
        RecyclerView.Adapter<ChatMessageRecyclerViewAdapter.ViewHolder>() {

        private val chatMessages = mutableListOf<ChatMessageModel>()

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        init {
            setChatMessageSnapshotListener(this)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ChatMessageRecyclerViewAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_message, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return chatMessages.count()
        }

        override fun onBindViewHolder(holder: ChatMessageRecyclerViewAdapter.ViewHolder, position: Int) {
            val message = chatMessages[position]
            holder.view.text_view_public_name.text = message.senderName
            holder.view.text_view_message.text = message.message
        }

        private fun getPosition(chatMessage: ChatMessageModel) = chatMessages.indexOf(chatMessage)

        fun insert(chatMessage: ChatMessageModel) {
            chatMessages.add(chatMessages.size, chatMessage)
            notifyItemInserted(chatMessages.size)
        }

        fun update(chatMessage: ChatMessageModel) {
            notifyItemChanged(getPosition(chatMessage))
        }

        fun delete(chatMessage: ChatMessageModel) {
            chatMessages.remove(chatMessage)
            notifyItemRemoved(getPosition(chatMessage))
        }
    }

    private fun setChatMessageSnapshotListener(chatMessageRecyclerViewAdapter: ChatMessageRecyclerViewAdapter) {
        listenerRegistration = collectionReference.addSnapshotListener { value, error ->
            if (error != null)
                println("$TAG: $error")
            else {
                for (change in value!!.documentChanges) {
                    when (change.type) {
                        DocumentChange.Type.ADDED ->
                            chatMessageRecyclerViewAdapter.insert(ChatMessageModel(change.document.data))
                        DocumentChange.Type.MODIFIED ->
                            chatMessageRecyclerViewAdapter.update(ChatMessageModel(change.document.data))
                        DocumentChange.Type.REMOVED ->
                            chatMessageRecyclerViewAdapter.delete(ChatMessageModel(change.document.data))
                        else -> {  }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "ChatFragment"

        const val ACTION_MAIN = "android.intent.action.MAIN"
    }
}
