package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity.Companion.CHAT_FRAGMENT_TAG
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.hashString
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.ChatMessageModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.PRModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_pr.*
import java.text.SimpleDateFormat
import java.util.*

class PRFragment(private val pr: PRModel? = null) : Fragment() {

    private var currentUserName = ""
    private var isFabOpen = false
    private lateinit var fabOpenAnimation: Animation
    private lateinit var fabCloseAnimation: Animation
    private lateinit var fabRotateForwardAnimation: Animation
    private lateinit var fabRotateBackwardAnimation: Animation

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
            val creationTime = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val roomName = hashString(currentUserName + pr?.publisher + creationTime).chunked(16)[0]
            createChatRoom(roomName, creationTime)
        }.setNegativeButton("취소") { _, _ -> }
            .create().show()
    }


    private fun createChatRoom(roomName: String, creationTime: String) {
        val chatMessage = ChatMessageModel()
        chatMessage.publicName = null
        chatMessage.message = "init"
        chatMessage.time = creationTime

        FirebaseFirestore.getInstance()
            .collection(CHAT)
            .document(roomName)
            .collection(CHAT_MESSAGES)
            .document("init").set(chatMessage)
            .addOnCompleteListener {  task ->
                if (task.isSuccessful) {
                    (activity as MainActivity).showToast("채팅방이 생성되었습니다.")
                    (activity as MainActivity)
                        .startFragment(ChatFragment(), R.id.main_activity_container_view, CHAT_FRAGMENT_TAG)
                } else {
                    (activity as MainActivity).showToast("채팅방 생성에 실패했습니다.")
                    println("$TAG: ${task.exception}")
                }
            }
    }

    companion object {
        const val TAG = "PRFragment"
    }
}