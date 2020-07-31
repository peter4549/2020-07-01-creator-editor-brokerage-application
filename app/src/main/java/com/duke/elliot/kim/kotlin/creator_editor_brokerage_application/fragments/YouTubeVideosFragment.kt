package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.YouTubeChannelsActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.dialog_fragments.PlaylistsDialogFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.VideoModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import kotlinx.android.synthetic.main.fragment_youtube_videos.*
import kotlinx.android.synthetic.main.fragment_youtube_videos.view.*
import kotlinx.android.synthetic.main.item_view_video.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class YouTubeVideosFragment(private val sourceId: String? = null,
                            private val allVideos: Boolean? = null,
                            private val playlistTitle: String? = null) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_youtube_videos, container, false)

        (activity as YouTubeChannelsActivity).setSupportActionBar(view.toolbar)
        (activity as YouTubeChannelsActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        if (playlistTitle == null)
            view.toolbar.title = "모든 비디오"
        else
            view.toolbar.title = playlistTitle

        if (sourceId != null && allVideos != null) {
            view.recycler_view.apply {
                adapter = VideosRecyclerViewAdapter(sourceId)
                layoutManager = LayoutManagerWrapper(requireContext(), 1)
            }
        } else {
            println("$TAG: channel id not found")
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        frame_layout_select_playlist.setOnClickListener {
            PlaylistsDialogFragment(sourceId).show(requireActivity().supportFragmentManager, TAG)
        }
    }

    inner class VideosRecyclerViewAdapter(sourceId: String) : RecyclerView.Adapter<VideosRecyclerViewAdapter.ViewHolder>() {

        private val videos = mutableListOf<VideoModel>()

        init {
            if (allVideos == true)
                setVideosByChannelId(sourceId)
            else if (allVideos == false)
                setVideosByPlaylistId(sourceId)
        }

        private fun setVideosByChannelId(channelId: String) {
            // 이게 아니도 비디오 아이템으로 읽어올것. 비디오 아이템에서 뽑아오면 된다. 만약 채널 목록이 없는 경우 전부 불러오도록.
            // 페이지 처리도 여기서.
            // 여기서 플레이리스트 카운트 받고, url을 다르게 설정할 것.
            val url = "https://www.googleapis.com/youtube/v3/search?key=${getString(R.string.google_api_key)}&" +
                    "part=snippet&channelId=$channelId"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    showToast(requireContext(), "비디오를 읽어오는데 실패했습니다. (4)")
                    println("$TAG: failed to get video")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            val map: Map<*, *>? =
                                Gson().fromJson(response.body?.string(), Map::class.java)
                            val items = (map?.get("items") as ArrayList<*>)
                            val videoIds = items.filter {
                                ((it as LinkedTreeMap<*, *>)["id"]
                                        as LinkedTreeMap<*, *>)["kind"] == "youtube#video"
                            }.map { ((it as LinkedTreeMap<*, *>)["id"] as LinkedTreeMap<*, *>)["videoId"] as String }

                            setVideosByIds(videoIds.joinToString(separator = ","))
                        } catch (e: Exception) {
                            showToast(requireContext(), "비디오를 읽어오는데 실패했습니다. 후에엥 (3)")
                            println("$TAG KINSTONE: ${e.message}")
                        }
                    } else {
                        showToast(requireContext(), "채널을 읽어오는데 실패했습니다. (4)")
                        println("$TAG: failed to get channel")
                    }
                }

            })
        }

        private fun setVideosByPlaylistId(playlistId: String) {
            val url = "https://www.googleapis.com/youtube/v3/playlistItems?key=${getString(R.string.google_api_key)}&" +
                    "part=contentDetails&playlistId=$playlistId"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    showToast(requireContext(), "비디오를 읽어오는데 실패했습니다. (4)")
                    println("$TAG: failed to get video")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            val map: Map<*, *>? =
                                Gson().fromJson(response.body?.string(), Map::class.java)
                            val items = map?.get("items") as ArrayList<*>
                            val videoIds = items.map {
                                ((it as LinkedTreeMap<*, *>)["contentDetails"]
                                        as LinkedTreeMap<*, *>)["videoId"] as String
                            }

                            setVideosByIds(videoIds.joinToString(separator = ","))
                        } catch (e: Exception) {
                            showToast(requireContext(), "비디오를 읽어오는데 실패했습니다. 후에엥 (3)")
                            println("$TAG KINSTONE: ${e.message}")
                        }
                    } else {
                        showToast(requireContext(), "채널을 읽어오는데 실패했습니다. (4)")
                        println("$TAG: failed to get channel")
                    }
                }

            })
        }

        private fun setVideosByIds(videoIds: String) {
            val url = "https://www.googleapis.com/youtube/v3/videos?key=${getString(R.string.google_api_key)}&" +
                    "part=snippet,statistics&id=$videoIds"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            val okHttpClient = OkHttpClient()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                try {
                    val map: Map<*, *>? =
                        Gson().fromJson(response.body?.string(), Map::class.java)
                    val items = map?.get("items") as ArrayList<*>

                    for (item in items) {
                        val id = (item as LinkedTreeMap<*, *>)["id"] as String
                        val snippet = item["snippet"] as LinkedTreeMap<*, *>
                        val channelId = snippet["channelId"] as String
                        val description = snippet["description"] as String
                        val publishTime = snippet["publishedAt"] as String
                        val title = snippet["title"] as String
                        val thumbnails = snippet["thumbnails"] as LinkedTreeMap<*, *>
                        val thumbnailUri = (thumbnails["default"] as LinkedTreeMap<*, *>)["url"] as String

                        this.videos.add(0, VideoModel(channelId, description, publishTime, thumbnailUri, title, id))
                        CoroutineScope(Dispatchers.Main).launch {
                            notifyItemInserted(0)
                        }
                    }


                } catch (e: Exception) {
                    showToast(requireContext(), "비디오를 읽어오는데 실패했습니다.")
                    println("$TAG KINSTONE: ${e.message}")
                }
            }
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): VideosRecyclerViewAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_view_video, parent, false)

            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return videos.size
        }

        override fun onBindViewHolder(holder: VideosRecyclerViewAdapter.ViewHolder, position: Int) {
            val video = videos[position]

            holder.view.text_view_description.text = video.description
            holder.view.text_view_publish_time.text = video.publishTime
            holder.view.text_view_title.text = video.title
            loadImage(holder.view.image_view, video.thumbnailUri)

            holder.view.setOnClickListener {
                val intent = Intent()

                intent.putExtra(KEY_THUMBNAIL_URI, video.thumbnailUri)
                intent.putExtra(KEY_VIDEO_ID, video.videoId)
                requireActivity().setResult(Activity.RESULT_OK, intent)
                requireActivity().finish()
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
        const val KEY_VIDEO_ID = "key_video_id"
    }
}
