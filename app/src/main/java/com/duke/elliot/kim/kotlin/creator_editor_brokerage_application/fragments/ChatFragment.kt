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
            getGroupNotificationKey() // test!
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

        FirebaseFirestore.getInstance()
            .collection(COLLECTION_CHAT)
            .document(chatRoomId)
            .collection(COLLECTION_CHAT_MESSAGES)
            .add(chatMessage)
            .addOnCompleteListener {  task ->
                if (task.isSuccessful) {
                    chatRoom = ChatRoomModel()
                    chatRoom?.creationTime = creationTime
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
                                showToast(requireContext(), "채팅방이 생성되었습니다.")
                                sendCloudMessage(chatRoom!!)
                            }
                        }

                    collectionReference = FirebaseFirestore.getInstance()
                        .collection(COLLECTION_CHAT).document(chatRoomId)
                        .collection(COLLECTION_CHAT_MESSAGES)

                    // setChatMessageListener() // 리사이클러뷰만 달면됨.
                    initRecyclerView()
                } else {
                    showToast(requireContext(), "채팅방 생성에 실패했습니다.")
                    println("${PrFragment.TAG}: ${task.exception}")
                }
            }
    }

    private fun sendMessage(message: String) { // 채팅메시지 쏘는 부분.
        val chatMessage = ChatMessageModel()

        chatMessage.senderName = currentUserPublicName
        chatMessage.message = message
        chatMessage.time = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())

        collectionReference.add(chatMessage).addOnCompleteListener {  task ->
            if (task.isSuccessful)
                println("$TAG: Message sent")
            else {
                showToast(requireContext(), "메시지 전송에 실패했습니다.")
                println("$TAG: ${task.exception}")
            }
        }
    }

    private fun getGroupNotificationKey() {
        val creationTime = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val url = "https://fcm.googleapis.com/fcm/notification"
        val notificationModel = NotificationModel()

        notificationModel.operation = "create"
        if (chatRoom == null)
            notificationModel.notification_key_name =
                hashString(listOf(currentUserId, publisherId).joinToString() + creationTime).chunked(16)[0]
        else
            notificationModel.notification_key_name =
                hashString(chatRoom!!.userIds.joinToString() + creationTime).chunked(16)[0]


        println("HOXY?HOX" + notificationModel.notification_key_name)
        //notificationModel.registration_ids = chatRoom?.userIds?.toList()
        notificationModel.registration_ids =
            arrayOf("cqTS8NH-RPKRsasE1W2lEe:APA91bE2ozNJaJra6jykYV8pMrGE6kbKgZ1eu7M-O7NUcMAVILfd3pMov_wWYZRcMskaxEreQv0fMv1fX418FUXqyQ4pV2CUNlmrefj1UIHhDYeLKMOrTQaIZ3VfetHuv5Q3EOctwQoM")

        val requestBody =
            RequestBody.create(MediaType.parse("application/json; charset=utf8"),
                Gson().toJson(notificationModel))

        println("DRACULAA: " + Gson().toJson(notificationModel).toString())

        val request = Request.Builder().header("Content-Type", "application/json")
            .addHeader("Authorization", API_KEY)
            .addHeader("project_id", SENDER_ID)  // MainActivity.currentUser?.pushToken)
            .url(url)
            .post(requestBody)
            .build()

        println("$TAG : THISFORRESP" + notificationModel.registration_ids.toString())

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                //showToast(requireContext(), "notifycation key")
                println("$TAG ${e?.message}")
            }

            override fun onResponse(response: Response?) {
                //showToast(requireContext(), "notifycation key. getodaze!" + response.toString())
                println("$TAG::thisisforresultgetTok ${response?.body()?.string()}") // 뭐날라오는지 확인. 이걸로 완료.
                println("HOWABOUThis111 " + response?.message())
                println("HOWABOUThis222 " + response!!)
            }
        })
    }

    private fun sendCloudMessage(chatRoom: ChatRoomModel) {
        // 그룹인지 아닌지 판단해서 보낼 것.
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS).whereEqualTo(
                UserModel.KEY_ID, publisherId)  // 멤버들한테 다 보내야함. 이부분은 단체 채팅부분에서 수정할것. 이게 아니라 챗 룸의 멤버아이디..로 서칭.
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val map = task.result?.documents?.get(0)?.data
                    if (map != null) {
                        val pushToken = map[KEY_PUSH_TOKEN] as String // 상대방한테갈 푸시토큰. 상대방의 푸시 토큰.
                        sendPushMessage(pushToken, chatRoom.roomId)
                        println("$TAG: Publisher ID found")
                    } else
                        showToast(requireContext(), "퍼블리셔를 찾을 수 없습니다.")
                } else {
                    showToast(requireContext(), "퍼블리셔를 찾을 수 없습니다.")
                    println("$TAG: ${task.exception}")
                }
            }
    }

    private fun sendPushMessage(pushToken: String, roomId: String) { // 이 함수, 통일할 것.
        val url = "https://fcm.googleapis.com/fcm/send"
        val text = "${currentUserPublicName}님께서 대화를 요청하셨습니다."
        val cloudMessage = CloudMessageModel()

        cloudMessage.to = pushToken
        cloudMessage.notification.title = "대화 요청 알림"
        cloudMessage.notification.text = text

        cloudMessage.data.message = "최신메시지"
        cloudMessage.data.roomId = roomId
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
                showToast(requireContext(), "채팅 요청에 실패했습니다.")
                println("${PrFragment.TAG}: ${e?.message}")
            }

            override fun onResponse(response: Response?) {
                showToast(requireContext(), "채팅을 요청했습니다.")
                println("$TAG: ${response?.body()?.string()}") // 뭐날라오는지 확인.
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
    }
}
