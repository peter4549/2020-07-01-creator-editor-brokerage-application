package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.youtube

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.REQUEST_CODE_YOUTUBE_PLAYER
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.dialog_fragments.ThreeViewsDialogFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.VideoModel

class SelectRegisterOrPlayVideoDialogFragment(private val video: VideoModel? = null):
    ThreeViewsDialogFragment() {

    private val firstButtonClickListener = View.OnClickListener {
        (requireActivity() as YouTubeChannelsActivity).registerVideo(video!!)
        dismiss()
    }

    private val secondButtonClickListener = View.OnClickListener {
        startYouTubePlayerActivity(video!!)
        dismiss()
    }

    private fun startYouTubePlayerActivity(video: VideoModel) {
        val intent = Intent(requireActivity(), YouTubePlayerActivity::class.java)
        intent.putExtra(KEY_VIDEO, video)
        requireActivity().startActivityForResult(intent, REQUEST_CODE_YOUTUBE_PLAYER)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        setFirstButton("등록", null, firstButtonClickListener)
        setSecondButton("동영상 재생", null, secondButtonClickListener)
        return dialog
    }

    companion object {
        const val ACTION_YOUTUBE_PLAYER = "select.register.or.play.video.dialog.fragment.action.youtube.player"
        const val KEY_VIDEO = "key_video"
    }
}