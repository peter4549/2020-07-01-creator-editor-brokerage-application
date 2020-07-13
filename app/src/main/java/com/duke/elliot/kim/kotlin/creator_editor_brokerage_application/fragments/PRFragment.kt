package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity.Companion.CHAT_FRAGMENT_TAG
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity.Companion.currentUser
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.hashString
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.*
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.squareup.okhttp.*
import kotlinx.android.synthetic.main.fragment_pr.*
import kotlinx.coroutines.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PRFragment(private val pr: PRModel? = null) : Fragment() {

    private lateinit var fabOpenAnimation: Animation
    private lateinit var fabCloseAnimation: Animation
    private lateinit var fabRotateForwardAnimation: Animation
    private lateinit var fabRotateBackwardAnimation: Animation
    private var currentUserPublicName = ""
    private var existingChatRooms: List<ChatRoomModel>? = null
    private var isFabOpen = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fabOpenAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_fab_open)
        fabCloseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_fab_close)
        fabRotateForwardAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate_forward)
        fabRotateBackwardAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate_backward)

        return inflater.inflate(R.layout.fragment_pr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (pr == null) {
            (activity as MainActivity).showToast("PR 정보를 불러오는데 실패했습니다.")
            (activity as MainActivity).onBackPressed()
        }

        text_view_name.text = pr?.title

        if (pr?.userId == currentUser?.uid) {
            disableFab()
        } else {
            fab_unfold.setOnClickListener {
                animateFab()
            }

            fab_chat.setOnClickListener {
                confirmToStartChatting()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currentUserPublicName = (activity as MainActivity).currentUserModel.publicName
    }

    private fun disableFab() {
        fab_unfold.visibility = View.GONE
        fab_chat.visibility = View.GONE
        fab_interest.visibility = View.GONE
        fab_star.visibility = View.GONE
    }

    private fun animateFab() {
        if (isFabOpen) {
            fab_unfold.startAnimation(fabRotateBackwardAnimation)
            fab_chat.startAnimation(fabCloseAnimation)
            fab_interest.startAnimation(fabCloseAnimation)
            fab_star.startAnimation(fabCloseAnimation)
            fab_chat.isClickable = false
            fab_interest.isClickable = false
            fab_star.isClickable = false
        } else {
            fab_unfold.startAnimation(fabRotateForwardAnimation)
            fab_chat.startAnimation(fabOpenAnimation)
            fab_interest.startAnimation(fabOpenAnimation)
            fab_star.startAnimation(fabOpenAnimation)
            fab_chat.isClickable = true
            fab_interest.isClickable = true
            fab_star.isClickable = true
        }

        isFabOpen = !isFabOpen
    }

    private fun confirmToStartChatting() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("채팅 시작")
        builder.setMessage("채팅을 시작합니다.")
        builder.setPositiveButton("확인") { _, _ ->
            val job = Job() as Job
            CoroutineScope(Dispatchers.IO + job).launch {
                checkExistingRooms()
            }
        }.setNegativeButton("취소") { _, _ -> }
            .create().show()
    }

    private fun checkExistingRooms() {
        val myUid = currentUser?.uid.toString()

        FirebaseFirestore.getInstance()
            .collection(COLLECTION_CHAT).whereEqualTo(KEY_CHAT_ROOM_PUBLISHER_ID, pr!!.userId)
            .whereArrayContains(KEY_CHAT_ROOM_MEMBER_IDS, myUid)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    existingChatRooms = task.result?.documents?.map { ChatRoomModel(it.data!!) }
                } else {
                    println("$TAG: ${task.exception}")
                }

                val creationTime = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
                val room = hashString(currentUserPublicName + pr.publisher + creationTime).chunked(16)[0]

                if (existingChatRooms != null) {
                    when {
                        existingChatRooms!!.isEmpty() -> createChatRoom(room, creationTime)
                        existingChatRooms!!.count() == 1 -> confirmChatRoomCreation(
                            ONE_EXISTING_CHAT_ROOM, room, creationTime)
                        else -> confirmChatRoomCreation(SEVERAL_EXISTING_CHAT_ROOM, room , creationTime)
                    }
                } else
                    createChatRoom(room, creationTime)
            }
    }

    private fun confirmChatRoomCreation(flag: Int, room: String, creationTime: String) {
        var message  = ""
        var neutralButtonText = ""

        if (flag == ONE_EXISTING_CHAT_ROOM) {
            message = "이미 상대방과의 채팅방이 존재합니다.\n" +
                    "새로운 채팅방을 생성하시겠습니까?"
            neutralButtonText = "기존 채팅방으로 이동"
        } else if (flag == SEVERAL_EXISTING_CHAT_ROOM) {
            message = "이미 상대방과의 채팅방 여러개 존재합니다.\n" +
                    "새로운 채팅방을 생성하시겠습니까?"
            neutralButtonText = "채팅방 목록으로 이동"
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("채팅방 생성")
        builder.setMessage(message)
        builder.setPositiveButton("생성") { _, _ -> createChatRoom(room, creationTime) }
            .setNeutralButton(neutralButtonText) { _, _ ->
                if (flag == ONE_EXISTING_CHAT_ROOM)
                    enterChatRoom(existingChatRooms!![0])
                else if (flag == SEVERAL_EXISTING_CHAT_ROOM)
                    moveToChatRoomsFragment()
            }.create().show()
    }

    private fun createChatRoom(room: String, creationTime: String) {
        val chatMessage = ChatMessageModel()  // 챗 메시지에 시스템 챗으로, 어댑터에서 퍼블리셔가 널이면
        // 시스템 알림 메시지로 처리.
        // 마지막 챗에도 업데이트 하지 않을 것.
        chatMessage.publicName = null
        chatMessage.message = "init"
        chatMessage.time = creationTime

        FirebaseFirestore.getInstance()
            .collection(COLLECTION_CHAT)
            .document(room)
            .collection(COLLECTION_CHAT_MESSAGES)
            .document("init").set(chatMessage)
            .addOnCompleteListener {  task ->
                if (task.isSuccessful) {
                    CoroutineScope(Dispatchers.Main).launch {
                        (activity as MainActivity).showToast("채팅방이 생성되었습니다.")
                        sendFcm(room)
                    }

                    val chatRoom = ChatRoomModel()
                    chatRoom.roomId = room
                    chatRoom.memberIds = mutableListOf(currentUser?.uid, pr?.userId)
                    chatRoom.creationTime = creationTime
                    chatRoom.publisherId = pr?.userId!!
                    chatRoom.memberPublicNames = mutableListOf(currentUserPublicName, pr.publisher)

                    FirebaseFirestore.getInstance()
                        .collection(COLLECTION_CHAT)
                        .document(room).set(chatRoom)

                    updateUserChatRooms(room)  // Update chat room name information in the user model
                    (activity as MainActivity)
                        .startFragment(ChatFragment(chatRoom), R.id.relative_layout_activity_main, CHAT_FRAGMENT_TAG)
                } else {
                    showToast(requireContext(), "채팅방 생성에 실패했습니다.")
                    println("$TAG: ${task.exception}")
                }
            }
    }

    private fun enterChatRoom(chatRoom: ChatRoomModel) {
        (activity as MainActivity)
            .startFragment(ChatFragment(chatRoom), R.id.relative_layout_activity_main, CHAT_FRAGMENT_TAG)
    }

    private fun moveToChatRoomsFragment() {

    }

    private fun updateUserChatRooms(room: String) {
        val map = mutableMapOf<String, Any>()

        (activity as MainActivity).currentUserModel.myChatRooms.add(room)
        map[USER_MY_CHAT_ROOMS] = (activity as MainActivity).currentUserModel.myChatRooms
        FirebaseFirestore.getInstance().collection(USERS)
            .document(currentUser!!.uid).update(map).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("$TAG: Chat rooms updated")
                } else {
                    (activity as MainActivity).showToast("채팅방 정보 업데이트에 실패했습니다.")
                    println("$TAG: ${task.exception}")
                }
            }
    }

    private fun sendFcm(roomId: String) {
        FirebaseFirestore.getInstance()
            .collection(USERS).whereEqualTo(KEY_USER_ID, pr?.userId)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val map = task.result?.documents?.get(0)?.data
                    if (map != null) {
                        val pushToken = map[KEY_PUSH_TOKEN] as String
                        sendPushMessage(pushToken, roomId)
                        println("$TAG: Publisher ID found")
                    } else
                        showToast(requireContext(), "퍼블리셔를 찾을 수 없습니다.")
                } else {
                    showToast(requireContext(), "퍼블리셔를 찾을 수 없습니다.")
                    println("$TAG: ${task.exception}")
                }
        }
    }

    private fun sendPushMessage(pushToken: String, roomId: String) {
        val gson = Gson()
        val text = "${currentUserPublicName}님께서 대화를 요청하셨습니다."
        val notificationModel = CloudMessageModel()
        notificationModel.to = pushToken

        println("FUCKWHAT: " + pushToken)

        notificationModel.notification.title = "대화 요청 알림"
        notificationModel.notification.text = text
        notificationModel.data.roomId = roomId

        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel))
        val request = Request.Builder().header("Content-Type", "application/json")
            .addHeader("Authorization",
                "key=AAAAi-iss7Q:APA91bEBStlhkp7asJn72PEZUtMvql_1oLb_LC5DfJ-RwpaRUQYrPJR1WEGAZXqCPQ5eqqBdQtdA5MM0J2oJQSSNi27BxiNCuIaaySVyXhYr1mMdIlJoCFtJboy2ydOjfLy59Fj_Khmu")
            .url("https://fcm.googleapis.com/fcm/send")
            .post(requestBody)
            .build()

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                showToast(requireContext(), "채팅 요청에 실패했습니다.")
                println("$TAG: ${e?.message}")
            }

            override fun onResponse(response: Response?) {
                showToast(requireContext(), "채팅을 요청했습니다.")
                println("$TAG: $response")
            }
        })
    }

    companion object {
        const val TAG = "PRFragment"

        const val ONE_EXISTING_CHAT_ROOM = 0
        const val SEVERAL_EXISTING_CHAT_ROOM = 1
    }
}