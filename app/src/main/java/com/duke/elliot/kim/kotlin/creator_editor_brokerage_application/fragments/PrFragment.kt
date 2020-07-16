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
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.*
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.hashString
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_pr.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class PrFragment : Fragment() {

    private lateinit var pr: PrModel
    private lateinit var fabOpenAnimation: Animation
    private lateinit var fabCloseAnimation: Animation
    private lateinit var fabRotateForwardAnimation: Animation
    private lateinit var fabRotateBackwardAnimation: Animation
    private var existingChatRooms: List<ChatRoomModel>? = null
    private var isFabOpen = false

    fun setPr(pr: PrModel) {
        this.pr = pr
    }

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

        text_view_name.text = pr.title

        if (pr.publisherId == FirebaseAuth.getInstance().currentUser?.uid) {
            disableFab()
        } else {
            fab_unfold.setOnClickListener {
                if (MainActivity.currentUser == null)
                    (activity as MainActivity).requestProfileCreation()
                else if (!MainActivity.currentUser!!.verified)
                    (activity as MainActivity).requestAuthentication()
                else
                    animateFab()
            }

            fab_chat.setOnClickListener {
                confirmToStartChatting()
            }
        }
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
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_CHAT).whereEqualTo(
                ChatRoomModel.KEY_PUBLISHER_ID, pr.publisherId)
            .whereArrayContains(ChatRoomModel.KEY_USER_IDS, FirebaseAuth.getInstance().currentUser?.uid.toString())
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    existingChatRooms = task.result?.documents?.map { ChatRoomModel(it.data!!) }
                } else {
                    println("$TAG: ${task.exception}")
                }

                val creationTime = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
                val room = hashString(FirebaseAuth.getInstance().currentUser?.uid +
                        pr.publisherId + creationTime).chunked(16)[0]

                if (existingChatRooms != null) {
                    when {
                        existingChatRooms!!.isEmpty() -> enterNewChatRoom()
                        existingChatRooms!!.count() == 1 -> confirmChatRoomCreation(
                            ONE_EXISTING_CHAT_ROOM, room, creationTime)
                        else -> confirmChatRoomCreation(SEVERAL_EXISTING_CHAT_ROOM, room , creationTime)
                    }
                } else
                    enterNewChatRoom()
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
        builder.setPositiveButton("생성") { _, _ -> enterNewChatRoom() }
            .setNeutralButton(neutralButtonText) { _, _ ->
                if (flag == ONE_EXISTING_CHAT_ROOM)
                    enterExistingChatRoom(existingChatRooms!![0])
                else if (flag == SEVERAL_EXISTING_CHAT_ROOM)
                    moveToChatRoomsFragment()
            }.create().show()
    }



    private fun enterExistingChatRoom(chatRoom: ChatRoomModel) {
        (activity as MainActivity)
            .startFragment(ChatFragment(chatRoom, pr), R.id.relative_layout_activity_main, CHAT_FRAGMENT_TAG)
    }

    private fun enterNewChatRoom() {
        (activity as MainActivity)
            .startFragment(ChatFragment(pr = pr), R.id.relative_layout_activity_main, CHAT_FRAGMENT_TAG)
    }

    private fun moveToChatRoomsFragment() {

    }

    companion object {
        const val TAG = "PRFragment"

        const val ONE_EXISTING_CHAT_ROOM = 0
        const val SEVERAL_EXISTING_CHAT_ROOM = 1
    }
}