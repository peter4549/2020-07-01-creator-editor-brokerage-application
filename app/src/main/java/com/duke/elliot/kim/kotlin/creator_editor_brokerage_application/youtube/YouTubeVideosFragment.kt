package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.youtube

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.ErrorHandler
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.ResponseFailedException
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.VideoModel
import kotlinx.android.synthetic.main.fragment_youtube_videos.*
import kotlinx.android.synthetic.main.fragment_youtube_videos.view.*
import kotlinx.android.synthetic.main.item_view_video.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class YouTubeVideosFragment(private val sourceId: String? = null,
                            private val isAllVideos: Boolean? = null,
                            private val playlistTitle: String? = null) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_youtube_videos, container, false)

        (activity as YouTubeChannelsActivity).setSupportActionBar(view.toolbar)
        (activity as YouTubeChannelsActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        if (playlistTitle == null)
            view.toolbar.title = getString(R.string.all_videos)
        else
            view.toolbar.title = playlistTitle

        if (sourceId != null && isAllVideos != null) {
            view.recycler_view.apply {
                adapter = VideosRecyclerViewAdapter(sourceId)
                layoutManager = LayoutManagerWrapper(requireContext(), 1)
            }
        } else {
            ErrorHandler.errorHandling(requireContext(), Exception("channel id not found"),
                getString(R.string.channel_id_not_found))
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        frame_layout_select_playlist.setOnClickListener {
            PlaylistsDialogFragment(
                sourceId
            ).show(requireActivity().supportFragmentManager,
                TAG
            )
        }
    }

    inner class VideosRecyclerViewAdapter(sourceId: String) : RecyclerView.Adapter<VideosRecyclerViewAdapter.ViewHolder>() {

        private val youTubeDataApi = (requireActivity() as YouTubeChannelsActivity).youTubeDataApi
        private var videos = mutableListOf<VideoModel>()

        init {
            if (isAllVideos == true)
                setVideosByChannelId(sourceId)
            else if (isAllVideos == false)
                setVideosByPlaylistId(sourceId)
        }

        private fun setVideosByChannelId(channelId: String) {
            val request = youTubeDataApi.getVideosRequestByChannelId(channelId)
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    ErrorHandler.errorHandling(requireContext(), e,
                        getString(R.string.failed_to_load_videos))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            val videoIds = youTubeDataApi.getVideoIdsFromResponse(response, true)
                            setVideosByIds(videoIds.joinToString(separator = ","))
                        } catch (e: Exception) {
                            ErrorHandler.errorHandling(requireContext(), e,
                                getString(R.string.failed_to_load_videos))
                        }
                    } else
                        ErrorHandler.errorHandling(requireContext(),
                            ResponseFailedException("failed to load channel", response),
                            getString(R.string.failed_to_load_videos))
                }
            })
        }

        private fun setVideosByPlaylistId(playlistId: String) {
            val request = youTubeDataApi.getVideosRequestByPlaylistId(playlistId)
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    ErrorHandler.errorHandling(requireContext(), e,
                        getString(R.string.failed_to_load_videos))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            val videoIds =
                                youTubeDataApi.getVideoIdsFromResponse(response, false)
                            setVideosByIds(videoIds.joinToString(separator = ","))
                        } catch (e: Exception) {
                            ErrorHandler.errorHandling(requireContext(), e,
                                getString(R.string.failed_to_load_videos))
                        }
                    } else
                        ErrorHandler.errorHandling(requireContext(),
                            ResponseFailedException("failed to load videos" ,response),
                            getString(R.string.failed_to_load_videos))
                }
            })
        }

        private fun setVideosByIds(videoIds: String) {
            val request = youTubeDataApi.getVideosRequestByIds(videoIds)
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    ErrorHandler.errorHandling(requireContext(),
                        e, getString(R.string.failed_to_load_videos))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            this@VideosRecyclerViewAdapter.videos = youTubeDataApi.getVideosFromResponse(response)
                            CoroutineScope(Dispatchers.Main).launch {
                                notifyItemInserted(0)
                            }
                        } catch (e: Exception) {
                            ErrorHandler.errorHandling(requireContext(),
                                e, getString(R.string.failed_to_load_videos))
                        }
                    }
                }
            })
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_view_video, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return videos.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val video = videos[position]

            holder.view.text_view_description.text = video.description
            holder.view.text_view_publish_time.text = video.publishTime
            holder.view.text_view_title.text = video.title
            loadImage(holder.view.image_view, video.thumbnailUri)

            holder.view.setOnClickListener {
                SelectRegisterOrPlayVideoDialogFragment(video)
                    .show(requireActivity().supportFragmentManager, tag)
            }
        }

        private fun loadImage(imageView: ImageView, imageUri: String) {
            Glide.with(imageView.context)
                .load(imageUri)
                .error(R.drawable.ic_sentiment_dissatisfied_grey_24dp)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        }
    }

    companion object {
        private const val TAG = "YouTubeVideosFragment"

        const val KEY_THUMBNAIL_URI = "key_thumbnail_uri"
        const val KEY_VIDEO = "key_video"
    }
}
