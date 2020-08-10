package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.youtube

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.ErrorHandler
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.PrFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.VideoModel
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import kotlinx.android.synthetic.main.activity_you_tube_player.*
import java.lang.Exception

class YouTubePlayerActivity : YouTubeBaseActivity() {

    private lateinit var video: VideoModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_you_tube_player)

        video = intent.getSerializableExtra(SelectRegisterOrPlayVideoDialogFragment.KEY_VIDEO)
                as VideoModel

        text_view_title.text = video.title
        text_view_description.text = video.description
        if (intent.getBooleanExtra(PrFragment.KEY_IS_FROM_PR_FRAGMENT, false))
            frame_layout_register.setOnClickListener {
                val intent = Intent()
                intent.putExtra(SelectRegisterOrPlayVideoDialogFragment.KEY_VIDEO, video)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        else
            frame_layout_register.visibility = View.GONE

        youtube_player_view.initialize(TAG, object: YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                provider: YouTubePlayer.Provider?,
                player: YouTubePlayer?,
                wasRestored: Boolean
            ) {
                if (!wasRestored)
                    player?.cueVideo(video.videoId)

                player?.setPlayerStateChangeListener(object: YouTubePlayer.PlayerStateChangeListener {
                    override fun onAdStarted() {  }

                    override fun onLoading() {  }

                    override fun onVideoStarted() {  }

                    override fun onLoaded(videoId: String?) {
                        player.play()
                    }

                    override fun onVideoEnded() {  }

                    override fun onError(reason: YouTubePlayer.ErrorReason?) {
                        ErrorHandler.errorHandling(this@YouTubePlayerActivity, Exception(reason.toString()),
                            getString(R.string.failed_to_load_video))
                    }
                })
            }

            override fun onInitializationFailure(
                provider: YouTubePlayer.Provider?,
                result: YouTubeInitializationResult?
            ) {
                ErrorHandler.errorHandling(this@YouTubePlayerActivity, Exception(result.toString()),
                    getString(R.string.player_initialization_failure_message))
            }
        })
    }

    companion object {
        private const val TAG = "YouTubePlayerActivity"
    }
}
