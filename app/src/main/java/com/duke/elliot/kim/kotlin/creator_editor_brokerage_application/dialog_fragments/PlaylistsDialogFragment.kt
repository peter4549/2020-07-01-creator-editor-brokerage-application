package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.YouTubeChannelsActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.PlaylistModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import kotlinx.android.synthetic.main.dialog_fragment_playlists.*
import kotlinx.android.synthetic.main.item_view_playlist.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class PlaylistsDialogFragment(private val channelId: String? = null): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())

        dialog.setContentView(R.layout.dialog_fragment_playlists)
        dialog.findViewById<TextView>(R.id.text_view_all_videos).setOnClickListener {
            if (channelId != null) {
                (activity as YouTubeChannelsActivity).startVideosFragment(channelId, true)
                dismiss()
            } else {

            }
        }

        if (channelId != null) {
            dialog.recycler_view.apply {
                adapter = PlaylistsRecyclerViewAdapter(channelId)
                layoutManager = LayoutManagerWrapper(requireContext(), 1)
            }
        } else {
            // 에러 보고.
        }

        return dialog
    }

    inner class PlaylistsRecyclerViewAdapter(channelId: String) :
        RecyclerView.Adapter<PlaylistsRecyclerViewAdapter.ViewHolder>() {

        private val playlists = mutableListOf<PlaylistModel>()

        init {
            getPlaylistsByChannelId(channelId)
        }

        private fun getPlaylistsByChannelId(channelId: String) {
            val url = "https://www.googleapis.com/youtube/v3/playlists?key=${getString(R.string.google_api_key)}&" +
                    "part=snippet&channelId=$channelId"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    showToast(requireContext(), "재생목록을 불러오는데 실패했습니다.")
                    println("$TAG: failed to get playlists") // 이런거 전부 에러 핸들러 클래스에서 처리할 것.
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            val map: Map<*, *>? =
                                Gson().fromJson(response.body?.string(), Map::class.java)
                            val items = map?.get("items") as ArrayList<*>
                            for (item in items) {
                                val id = (item as LinkedTreeMap<*, *>)["id"] as String
                                val snippet =
                                    (item as LinkedTreeMap<*, *>)["snippet"] as LinkedTreeMap<*, *>
                                val title = snippet["title"] as String
                                val description = snippet["description"] as String
                                val thumbnailUri = ((snippet["thumbnails"]
                                        as LinkedTreeMap<*, *>)["default"]
                                        as LinkedTreeMap<*, *>)["url"] as String
                                playlists.add(PlaylistModel(description, id, thumbnailUri, title))
                            }

                            CoroutineScope(Dispatchers.Main).launch {
                                notifyDataSetChanged()
                            }
                        } catch (e: Exception) {
                            showToast(requireContext(), "재생목록을 불러오는데 실패했습니다. (3)")
                            println("$TAG KINSTONE: ${e.message}")
                        }
                    } else {
                        showToast(requireContext(), "재생목록을 불러오는데 실패했습니다. (4)")
                        println("$TAG: failed to get video")
                    }
                }
            })
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PlaylistsRecyclerViewAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_view_playlist, parent, false)

            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return playlists.size
        }

        override fun onBindViewHolder(holder: PlaylistsRecyclerViewAdapter.ViewHolder, position: Int) {
            val playlist = playlists[position]

            holder.view.text_view_title.text = playlist.title
            holder.view.text_view_description
            loadImage(holder.view.image_view, playlist.thumbnailUri)

            holder.view.setOnClickListener {
                (activity as YouTubeChannelsActivity).startVideosFragment(playlist.id, false, playlist.title)
                dismiss()
            }
        }

        private fun getVideosOnPlaylist(playlistId: String) {
            //

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
        private const val TAG = "PlaylistDialogFragment"
    }
}