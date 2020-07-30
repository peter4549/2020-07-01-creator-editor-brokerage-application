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
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.VideoModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import kotlinx.android.synthetic.main.fragment_youtube_videos.view.*
import kotlinx.android.synthetic.main.item_view_video.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class YouTubeVideosFragment(private val channelId: String? = null) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_youtube_videos, container, false)

        if (channelId != null) {
            view.recycler_view.apply {
                adapter = VideosRecyclerViewAdapter(channelId)
                layoutManager = LayoutManagerWrapper(requireContext(), 1)
            }
        } else {
            println("$TAG: channel id not found")
        }
        return view
    }

    inner class VideosRecyclerViewAdapter(channelId: String) : RecyclerView.Adapter<VideosRecyclerViewAdapter.ViewHolder>() {

        private val videos = mutableListOf<VideoModel>()

        init {
            getVideosByChannelId(channelId)
        }

        private fun getVideosByChannelId(channelId: String) {
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
                            for (videoId in videoIds)
                                getVideoById(videoId)

                            CoroutineScope(Dispatchers.Main).launch {
                                notifyDataSetChanged()
                            }
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

        private fun getVideoById(videoId: String) {
            println("CANCANCANCNA")
            val url = "https://www.googleapis.com/youtube/v3/videos?key=${getString(R.string.google_api_key)}&" +
                    "part=snippet,statistics&id=$videoId"
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
                    val items = (map?.get("items") as ArrayList<*>)[0] as LinkedTreeMap<*, *>
                    val id = items["id"] as String
                    val snippet = items["snippet"] as LinkedTreeMap<*, *>
                    val description = snippet["description"] as String
                    val publishTime = snippet["publishedAt"] as String
                    val title = snippet["title"] as String
                    val thumbnails = snippet["thumbnails"] as LinkedTreeMap<*, *>
                    val thumbnailUri = (thumbnails["default"] as LinkedTreeMap<*, *>)["url"] as String

                    this.videos.add(VideoModel(description, publishTime, thumbnailUri, title, id))
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
                .inflate(R.layout.item_view_channel, parent, false)

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
