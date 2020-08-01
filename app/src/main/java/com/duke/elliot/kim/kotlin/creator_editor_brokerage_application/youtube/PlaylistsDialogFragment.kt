package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.youtube

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
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.ErrorHandler
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
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
            (activity as YouTubeChannelsActivity).startVideosFragment(channelId!!, true)
            dismiss()
        }

        dialog.recycler_view.apply {
            adapter = PlaylistsRecyclerViewAdapter(channelId!!)
            layoutManager = LayoutManagerWrapper(requireContext(), 1)
        }

        return dialog
    }

    inner class PlaylistsRecyclerViewAdapter(channelId: String) :
        RecyclerView.Adapter<PlaylistsRecyclerViewAdapter.ViewHolder>() {

        private val playlists = mutableListOf<PlaylistModel>()
        private val youTubeDataApi: YouTubeDataApi

        init {
            getPlaylistsByChannelId(channelId)
            youTubeDataApi = (requireActivity() as YouTubeChannelsActivity).youTubeDataApi
        }

        private fun getPlaylistsByChannelId(channelId: String) {

            val request = youTubeDataApi.getPlaylistsRequestByChannelId(channelId)

            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    ErrorHandler.errorHandling(requireContext(),
                        TAG,
                        Throwable(), e,
                        getString(R.string.failed_to_load_playlists))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            val map: Map<*, *>? =
                                Gson().fromJson(response.body?.string(), Map::class.java)
                            val items = youTubeDataApi.getItemsFromMap(map!!)
                            for (item in items) {
                                val playlist =
                                    youTubeDataApi.getPlaylistByItem(item as LinkedTreeMap<*, *>)
                                playlists.add(playlist)
                            }

                            CoroutineScope(Dispatchers.Main).launch {
                                notifyDataSetChanged()
                            }
                        } catch (e: Exception) {
                            ErrorHandler.errorHandling(requireContext(),
                                TAG,
                                Throwable(), e,
                                getString(R.string.failed_to_load_playlists))
                        }
                    } else {
                        ErrorHandler.errorHandling(requireContext(),
                            TAG,
                            Throwable(), Exception("response failed"),
                            getString(R.string.failed_to_load_playlists))
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
                .inflate(R.layout.item_view_playlist, parent, false)

            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return playlists.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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