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
import com.google.firebase.auth.FirebaseAuth
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

    private lateinit var chatMessageRecyclerViewAdapter: ChatMessageRecyclerViewAdapter
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

            setChatMessageListener()
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
        removeChatMessageListener() // 잘되는지 아직 모름. 아마 시작부분에 리사이클러뷰 초기화띠 하면 되지않을까.
        // 아니면 시작할 때, 채팅방 이름 비교해서 다르면 클리어.
        super.onStop()
    }

    private fun setChatMessageListener() {
        initRecyclerView()
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

    fun removeChatMessageListener() {
        listenerRegistration.remove()
    }

    private fun initRecyclerView() {
        chatMessageRecyclerViewAdapter = ChatMessageRecyclerViewAdapter(mutableListOf())
        recycler_view_chat_messages.apply {
            setHasFixedSize(true)
            adapter = chatMessageRecyclerViewAdapter
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
            .document("init").set(chatMessage)
            .addOnCompleteListener {  task ->
                if (task.isSuccessful) {
                    chatRoom = ChatRoomModel()
                    chatRoom?.creationTime = creationTime
                    chatRoom?.publisherId = publisherId
                    chatRoom?.publisherName = publisherName
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

                    setChatMessageListener()
                } else {
                    showToast(requireContext(), "채팅방 생성에 실패했습니다.")
                    println("${PrFragment.TAG}: ${task.exception}")
                }
            }
    }

    /*
    private fun updateUserChatRooms(room: String) {
        val map = mutableMapOf<String, Any>()

        MainActivity.currentUserModel?.myChatRooms?.add(room)
        map[KEY_USER_CHAT_ROOMS] = MainActivity.currentUserModel!!.myChatRooms
        FirebaseFirestore.getInstance().collection(COLLECTION_USERS)
            .document(MainActivity.currentUser!!.uid).update(map).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("$TAG: Chat rooms updated")
                } else {
                    showToast(requireContext(), "채팅방 정보 업데이트에 실패했습니다.")
                    println("$TAG: ${task.exception}")
                }
            }
    }

     */

    private fun sendMessage(message: String) {
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
                        println("${PrFragment.TAG}: Publisher ID found")
                    } else
                        showToast(requireContext(), "퍼블리셔를 찾을 수 없습니다.")
                } else {
                    showToast(requireContext(), "퍼블리셔를 찾을 수 없습니다.")
                    println("${PrFragment.TAG}: ${task.exception}")
                }
            }
    }

    private fun getGroupNotificationKey() {
        val url = "https://fcm.googleapis.com/fcm/notification"
        val notificationModel = NotificationModel()

        notificationModel.operation = "create"
        if (chatRoom == null)
            notificationModel.notification_key_name =
                hashString(listOf(currentUserId, publisherId).joinToString().chunked(16)[0])
        else
            notificationModel.notification_key_name =
                hashString(chatRoom!!.userIds.joinToString().chunked(16)[0])
        notificationModel.registration_ids = chatRoom?.userIds?.toList()

        val requestBody =
            RequestBody.create(MediaType.parse("application/json; charset=utf8"),
                Gson().toJson(notificationModel))
        val request = Request.Builder().header("Content-Type", "application/json")
            .addHeader("Authorization", API_KEY)
            .addHeader("project_id", PROJECT_ID)
            .url(url)
            .post(requestBody)
            .build()

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                showToast(requireContext(), "notifycation key")
                println("${PrFragment.TAG}: ${e?.message}")
            }

            override fun onResponse(response: Response?) {
                showToast(requireContext(), "notifycation key. getodaze!" + response.toString())
                println("${PrFragment.TAG}: $response") // 뭐날라오는지 확인.
            }
        })
    }

    private fun sendPushMessage(pushToken: String, roomId: String) {
        val gson = Gson()
        val url = "https://fcm.googleapis.com/fcm/send"
        val text = "${currentUserPublicName}님께서 대화를 요청하셨습니다."
        val cloudMessage = CloudMessageModel()

        cloudMessage.to = pushToken
        cloudMessage.notification.title = "대화 요청 알림"
        cloudMessage.notification.text = text

        cloudMessage.data.message = "최신메시지"
        cloudMessage.data.roomId = roomId
        cloudMessage.data.senderPublicName = currentUserPublicName

        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(cloudMessage))
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
                println("${PrFragment.TAG}: $response") // 뭐날라오는지 확인.
            }
        })
    }

    class ChatMessageRecyclerViewAdapter(private val chatMessages: MutableList<ChatMessageModel>) :
        RecyclerView.Adapter<ChatMessageRecyclerViewAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

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

    companion object {
        const val TAG = "ChatFragment"
    }
}
