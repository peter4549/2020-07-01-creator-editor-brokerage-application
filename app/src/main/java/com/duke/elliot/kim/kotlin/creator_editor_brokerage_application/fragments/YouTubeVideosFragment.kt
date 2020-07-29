package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R

class YouTubeVideosFragment : Fragment() {

    private lateinit var channelId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_you_tube_videos, container, false)
    }

    fun setChannelId(channelId: String) {
        this.channelId = channelId
    }



}
