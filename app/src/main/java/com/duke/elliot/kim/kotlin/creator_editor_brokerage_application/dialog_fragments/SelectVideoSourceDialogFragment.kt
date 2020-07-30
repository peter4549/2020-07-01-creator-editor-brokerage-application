package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity

class SelectVideoSourceDialogFragment: ThreeButtonsDialogFragment() {

    private val firstButtonClickListener = View.OnClickListener {
        (activity as MainActivity).writingFragment.openGallery()
        dismiss()
    }

    private val secondButtonClickListener = View.OnClickListener {
        (activity as MainActivity).writingFragment.openYouTubeChannels()
        dismiss()
    }

    private val thirdButtonClickListener = View.OnClickListener {
        (activity as MainActivity).writingFragment
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        setTitle("내 작품 등록")
        setFirstButton("기기에서 찾기", R.drawable.ic_phone_android_grey_32dp, firstButtonClickListener)
        setSecondButton("내 유튜브 채널에서 찾기", R.drawable.ic_youtube_red_32dp, secondButtonClickListener)
        setThirdButton("해당 작품 등록해제", R.drawable.ic_cancel_orange_32dp, thirdButtonClickListener)
        return dialog
    }
}