package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.youtube

import android.app.PendingIntent
import android.content.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.ErrorHandler
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.ResponseFailedException
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_USERS
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.ChannelModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.UserModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.services.CheckUrlJobIntentService
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import kotlinx.android.synthetic.main.activity_youtube_channels.*
import kotlinx.android.synthetic.main.item_view_channel.view.*
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import kotlin.Exception

class YouTubeChannelsActivity : AppCompatActivity() {

    lateinit var youTubeDataApi: YouTubeDataApi
    private lateinit var channelsRecyclerViewAdapter: ChannelsRecyclerViewAdapter
    private lateinit var user: UserModel
    private var connection: CustomTabsServiceConnection? = null

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == CheckUrlJobIntentService.ACTION_CORRECT_URL) {
                val code = intent.getStringExtra(CheckUrlJobIntentService.KEY_AUTHORIZATION_CODE)
                val selfIntent = Intent(this@YouTubeChannelsActivity, YouTubeChannelsActivity::class.java)

                selfIntent.action =
                    ACTION_NEW_INTENT
                selfIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                selfIntent.putExtra(KEY_AUTHORIZATION_CODE, code)
                startActivity(selfIntent)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == ACTION_NEW_INTENT) {
            val code = intent.getStringExtra(KEY_AUTHORIZATION_CODE) as String
            getAccessCode(code)
            // unbindCustomTabsService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_channels)

        user = MainActivity.currentUser!!
        youTubeDataApi = YouTubeDataApi(this)

        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            // 안내 메시지.
            showGuideToast()
        }

        frame_layout_add_channel.setOnClickListener {
            bindCustomTabsService()
        }

        channelsRecyclerViewAdapter = ChannelsRecyclerViewAdapter(user.channelIds)

        recycler_view.apply {
            adapter = channelsRecyclerViewAdapter
            layoutManager = LayoutManagerWrapper(this@YouTubeChannelsActivity, 1)
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
            IntentFilter(CheckUrlJobIntentService.ACTION_CORRECT_URL)
        )
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        unbindCustomTabsService()
        super.onDestroy()
    }

    private fun bindCustomTabsService() {
        if (connection != null)
            return

        connection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                name: ComponentName,
                client: CustomTabsClient
            ) {
                val builder = CustomTabsIntent.Builder()
                val icon = BitmapFactory.decodeResource(resources, android.R.drawable.ic_btn_speak_now)
                val title = getString(R.string.complete)

                builder.setToolbarColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                builder.setActionButton(icon, title,createPendingIntent(), true)
                builder.setExitAnimations(applicationContext, R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
                builder.setStartAnimations(applicationContext, R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)

                val customTabsIntent = builder.build()
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                customTabsIntent.intent.setPackage(CUSTOM_TAB_PACKAGE_NAME)
                customTabsIntent.launchUrl(this@YouTubeChannelsActivity, Uri.parse(
                    youTubeDataApi.getGoogleAuthorizationServerUrlWithParameters()
                ))
            }

            override fun onServiceDisconnected(componentName: ComponentName?) {
                println("$TAG: custom tabs service disconnected")
            }
        }

        if (CustomTabsClient.bindCustomTabsService(this,
                CUSTOM_TAB_PACKAGE_NAME, connection!!))
            println("$TAG: custom tabs service connected")
        else
            ErrorHandler.errorHandling(this,
                TAG,
                Throwable(), Exception("custom tabs service connection failed"),
                getString(R.string.authorization_server_connection_failed))
    }

    private fun unbindCustomTabsService() {
        if (connection == null)
            return
        else {
            this.unbindCustomTabsService()
            connection = null
        }
    }

    private fun showGuideToast() {
        val toast = Toast(this)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.duration = Toast.LENGTH_LONG
        toast.show()
    }

    private fun createPendingIntent() : PendingIntent {
        val intent = Intent(this, CheckUrlJobIntentService::class.java)
        return PendingIntent.getService(this, 0, intent, 0)
    }

    private fun getAccessCode(code: String) {
        val request = youTubeDataApi.getAuthorizationRequest(code)

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                    TAG,
                    Throwable(), e,
                    getString(R.string.failed_to_get_access_token))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val map: Map<*, *>? =
                        Gson().fromJson(response.body?.string(), Map::class.java)
                    getChannelId(map?.get("access_token") as String)
                } else
                    ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                        TAG,
                        Throwable(), Exception("response failed, failed to get access token"),
                        getString(R.string.failed_to_get_access_token))
            }
        })
    }

    private fun getChannelId(accessToken: String) {
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(youTubeDataApi.getChannelIdsRequestByAccessToken(accessToken))
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                        TAG,
                        Throwable(), e,
                        getString(R.string.failed_to_get_access_token))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            val map: Map<*, *>? =
                                Gson().fromJson(response.body?.string(), Map::class.java)
                            val items = map?.get("items") as ArrayList<*>
                            val channelId = (items[0] as LinkedTreeMap<*, *>)["id"] as String

                            if (channelId in user.channelIds) {
                                showToast(this@YouTubeChannelsActivity,
                                    getString(R.string.channel_already_registered))
                            } else {
                                channelsRecyclerViewAdapter.insert(channelId)
                                updateChannelId(channelId)
                            }
                        } catch (e: Exception) {
                            ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                                TAG,
                                Throwable(), e,
                                getString(R.string.failed_to_load_channels))
                        }
                    } else
                        ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                            TAG,
                            Throwable(), Exception("failed to get response"),
                            getString(R.string.failed_to_load_channels))
                }
            })
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateChannelId(channelId: String) {
        user.channelIds.add(channelId)
        FirebaseFirestore.getInstance().collection(COLLECTION_USERS).document(user.id)
            .update(hashMapOf(UserModel.KEY_CHANNEL_IDS to user.channelIds) as HashMap<String, Any>)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    MainActivity.currentUser!!.registeredOnPartners = true
                    println("$TAG: channel ids updated")
                }
                else
                    ErrorHandler.errorHandling(this,
                        TAG,
                        Throwable(), task.exception,
                        getString(R.string.failed_to_update_channel_ids))
            }
    }

    fun startVideosFragment(sourceId: String, allVideos: Boolean, playlistTitle: String? = null) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.anim_slide_in_left,
                R.anim.anim_slide_out_left,
                R.anim.anim_slide_in_right,
                R.anim.anim_slide_out_right
            ).replace(R.id.linear_layout_activity_youtube_channels,
                YouTubeVideosFragment(
                    sourceId,
                    allVideos,
                    playlistTitle
                ),
                YOUTUBE_VIDEOS_FRAGMENT_TAG
            ).commit()
    }

    inner class ChannelsRecyclerViewAdapter(channelIds: MutableList<String>) :
        RecyclerView.Adapter<ChannelsRecyclerViewAdapter.ViewHolder>() {

        private val channels = mutableListOf<ChannelModel>()

        init {
            insertChannelsByIds(channelIds)
        }

        private fun insertChannelsByIds(channelIds: MutableList<String>) {
            val request = youTubeDataApi.getChannelsRequestByIds(channelIds)
            val okHttpClient = OkHttpClient()
            var channelCount = 0
            okHttpClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                        TAG,
                        Throwable(), e,
                        getString(R.string.failed_to_load_channels))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val map = Gson().fromJson(response.body?.string(), Map::class.java)
                        try {
                            val items = youTubeDataApi.getItemsFromMap(map!!)

                            for (item in items) {
                                val channel =
                                    youTubeDataApi.getChannelByItem(item as LinkedTreeMap<*, *>)
                                channels.add(0, channel)

                                CoroutineScope(Dispatchers.Main).launch {
                                    notifyItemInserted(0)
                                }

                                ++channelCount
                            }

                            if (user.channelIds.count() > channelCount)
                                throw TypeCastException()
                        } catch (e: TypeCastException) {
                            val pageInfo = map["pageInfo"] as LinkedTreeMap<*, *>
                            val totalResults = pageInfo["totalResults"] as Double

                            if (totalResults == 0.0)
                                ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                                    TAG,
                                    Throwable(), e,
                                    getString(R.string.channels_not_found))
                            else{
                                val notFoundChannelsCount = user.channelIds.count() - channelCount
                                ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                                    TAG,
                                    Throwable(), e,
                                    notFoundChannelsCount.toString() + getString(R.string.n_channels_not_found))
                            }
                        } catch (e: Exception) {
                            ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                                TAG,
                                Throwable(), e,
                                getString(R.string.failed_to_load_channels))
                        }
                    } else {
                        ErrorHandler.errorHandling(
                            this@YouTubeChannelsActivity,
                            TAG,
                            Throwable(), ResponseFailedException("failed to load channels", response.body?.string()),
                            getString(R.string.failed_to_load_channels)
                        )
                    }
                }
            })
        }

        private fun insertChannelById(channelId: String) {
            val request = youTubeDataApi.getChannelRequestById(channelId)
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                        TAG,
                        Throwable(), Exception("failed to register channel"),
                        getString(R.string.failed_to_register_channel))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            val map: Map<*, *>? =
                                Gson().fromJson(response.body?.string(), Map::class.java)
                            val item = youTubeDataApi.getItemsFromMap(map!!)[0] as LinkedTreeMap<*, *>

                            channels.add(0, youTubeDataApi.getChannelByItem(item))

                            CoroutineScope(Dispatchers.Main).launch {
                                notifyItemInserted(0)
                            }
                        } catch (e: Exception) {
                            ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                                TAG,
                                Throwable(), e,
                                getString(R.string.failed_to_register_channel))
                        }
                    } else {
                        ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                            TAG,
                            Throwable(), Exception("failed to register channel"),
                            getString(R.string.failed_to_register_channel))
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
                .inflate(R.layout.item_view_channel, parent, false)

            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return channels.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val channel = channels[position]

            holder.view.text_view_title.text = channel.title
            holder.view.text_view_description
            loadImage(holder.view.image_view, channel.thumbnailUri)

            holder.view.setOnClickListener {
                PlaylistsDialogFragment(
                    channel.id
                ).show(this@YouTubeChannelsActivity.supportFragmentManager,
                    TAG
                )
            }
        }

        private fun loadImage(imageView: ImageView, imageUri: String) {
            Glide.with(imageView.context)
                .load(imageUri)
                .error(R.drawable.ic_sentiment_dissatisfied_grey_24dp)
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(CenterCrop())
                .into(imageView)
        }

        fun insert(channelId: String) {
            insertChannelById(channelId)
        }
    }

    companion object {
        private const val TAG = "YouTubeChannelsActivity"

        const val YOUTUBE_VIDEOS_FRAGMENT_TAG = "youtube_videos_fragment_tag"

        private const val ACTION_NEW_INTENT = "action_new_intent"
        private const val KEY_AUTHORIZATION_CODE = "key_authorization_code"

        private const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"
    }
}
