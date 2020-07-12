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
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.hashString
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
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
    private var currentUserName = ""
    private var existingChatRooms: List<ChatRoomModel>? = null
    private var isFabOpen = false

    var token: String? = null

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

        fab_unfold.setOnClickListener {
            animateFab()
        }

        fab_chat.setOnClickListener {
            confirmToStartChatting()
        }
    }

    override fun onResume() {
        super.onResume()
        currentUserName = (activity as MainActivity).currentUserModel.name
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
        val myUid = MainActivity.currentUser?.uid.toString()

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
                val room = hashString(currentUserName + pr.publisher + creationTime).chunked(16)[0]

                if (existingChatRooms != null) {
                    when {
                        existingChatRooms!!.isEmpty() -> createChatRoom(room, creationTime)
                        existingChatRooms!!.count() == 1 -> enterChatRoom(existingChatRooms!![0])
                        else -> moveToChatRoomsFragment()
                    }
                } else
                    createChatRoom(room, creationTime)
            }
    }

    private fun createChatRoom(room: String, creationTime: String) {
        val chatMessage = ChatMessageModel()
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
                    }

                    val chatRoom = ChatRoomModel()
                    chatRoom.roomId = room
                    chatRoom.memberIds = mutableListOf(MainActivity.currentUser?.uid, pr?.userId)
                    chatRoom.creationTime = creationTime
                    chatRoom.publisherId = pr?.userId!!

                    FirebaseFirestore.getInstance()
                        .collection(COLLECTION_CHAT)
                        .document(room).set(chatRoom)

                    updateUserChatRooms(room)  // Update chat room name information in the user model
                    (activity as MainActivity)
                        .startFragment(ChatFragment(chatRoom), R.id.main_activity_container_view, CHAT_FRAGMENT_TAG)
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        (activity as MainActivity).showToast("채팅방 생성에 실패했습니다.")
                    }
                    println("$TAG: ${task.exception}")
                }
            }
    }

    private fun enterChatRoom(chatRoom: ChatRoomModel) {
        (activity as MainActivity)
            .startFragment(ChatFragment(chatRoom), R.id.main_activity_container_view, CHAT_FRAGMENT_TAG)
    }

    private fun moveToChatRoomsFragment() {

    }

    private fun updateUserChatRooms(room: String) {
        val map = mutableMapOf<String, Any>()

        (activity as MainActivity).currentUserModel.myChatRooms.add(room)
        map[USER_MY_CHAT_ROOMS] = (activity as MainActivity).currentUserModel.myChatRooms
        FirebaseFirestore.getInstance().collection(USERS)
            .document(MainActivity.currentUser!!.uid).update(map).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("$TAG: Chat rooms updated")
                } else {
                    (activity as MainActivity).showToast("채팅방 정보 업데이트에 실패했습니다.")
                    println("$TAG: ${task.exception}")
                }
            }
    }

    private fun passPushTokenToServer() {

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                token = task.result?.token
                sendFcm()
                println("TOKENSUCEEEE!?==" + token)
            } else {
                println("TOKENFUCK")
            }
        }
    }


    private fun sendFcm() {
        val gson = Gson()
        val notificationModel = NotificationModel()
        //notificationModel.to = destinationUserModel.pushToken
        //notificationModel.to = "dCA5i44HSteSvUtEbCudBW:APA91bFWm_sdUQVifk9ONsDrHF3fRwk44b6b7rj2PVAroAbEORiW_8LB_A9gbndAHd0S87WQODoyprWRhLnMdW6ae0I51VdeP8J4IO5NeUqTwcN-SNMXRCz-Eo68RJwpHEqbmxsg2inp"//token!! // FirebaseInstanceId.getInstance().token!! // for test
        notificationModel.to = "dJ2nbpnRSRed4MQgdnK_yA:APA91bGXu3VPXMrHiuMA7y_RBo_b0TkO9-R5bA_Crw2bKm4EC75DvGSycf2HvtLTY1ZF8TK32RRqO5nUgpjU2z2GfMyvAYR7KhKoW4z_WrXDjVPK2M6m1Q1LNcPTHT6bdxqAiuBpGOj8"
        notificationModel.notification.title = "테스트 알림 타이틀"
        notificationModel.notification.text = (activity as MainActivity).currentUserModel.publicName


        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel))
        val request = Request.Builder().header("Content-Type", "application/json")
            .addHeader("Authorization", "key=AAAAi-iss7Q:APA91bEBStlhkp7asJn72PEZUtMvql_1oLb_LC5DfJ-RwpaRUQYrPJR1WEGAZXqCPQ5eqqBdQtdA5MM0J2oJQSSNi27BxiNCuIaaySVyXhYr1mMdIlJoCFtJboy2ydOjfLy59Fj_Khmu")
            .url("https://fcm.googleapis.com/fcm/send")
            .post(requestBody)
            .build()

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                println("FUCKINGMAN" + e?.message)
            }

            override fun onResponse(response: Response?) {
                println("RESSUC!?")
            }

        })

    }



    companion object {
        const val TAG = "PRFragment"
    }
}