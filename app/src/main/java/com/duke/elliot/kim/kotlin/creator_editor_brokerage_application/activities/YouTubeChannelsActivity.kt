package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities

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
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_USERS
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.dialog_fragments.PlaylistsDialogFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.YouTubeVideosFragment
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
import java.lang.Exception

class YouTubeChannelsActivity : AppCompatActivity() {

    private lateinit var channelsRecyclerViewAdapter: ChannelsRecyclerViewAdapter
    private lateinit var user: UserModel
    private var connection: CustomTabsServiceConnection? = null

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == CheckUrlJobIntentService.ACTION_CORRECT_URL) {
                val code = intent.getStringExtra(CheckUrlJobIntentService.KEY_AUTHORIZATION_CODE)
                val selfIntent = Intent(this@YouTubeChannelsActivity, YouTubeChannelsActivity::class.java)

                selfIntent.action = ACTION_NEW_INTENT
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
            unbindCustomTabsService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_channels)

        user = MainActivity.currentUser!!

        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            // 안내 메시지.
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
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY) // 필수. 위에꺼는 천천히ㅣ..
                customTabsIntent.intent.setPackage(CUSTOM_TAB_PACKAGE_NAME)
                customTabsIntent.launchUrl(this@YouTubeChannelsActivity, Uri.parse(GOOGLE_AUTHORIZATION_SERVER_URI))
            }

            override fun onServiceDisconnected(componentName: ComponentName?) {
                println("$TAG: custom tabs service disconnected")
            }
        }

        if (CustomTabsClient.bindCustomTabsService(this, CUSTOM_TAB_PACKAGE_NAME, connection!!))
            println("$TAG: custom tabs service connected")
        else {
            showToast(this, "인증서버 연결에 실패했습니다.")
            println("$TAG: failed to connect custom tabs service")
        }
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
        val view = layoutInflater.inflate(R.layout.custom_toast_guide, findViewById(R.id.linear_layout_custom_toast))

        val toast = Toast(this)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.duration = Toast.LENGTH_LONG
        //toast.view = view
        toast.show()
    }

    private fun createPendingIntent() : PendingIntent {
        val intent = Intent(this, CheckUrlJobIntentService::class.java)
        return PendingIntent.getService(this, 0, intent, 0)
    }

    private fun getAccessCode(code: String) {
        val url = "https://accounts.google.com/o/oauth2/token"

        val requestBody =
            FormBody.Builder().add("code", code)
                .add("client_id", CLIENT_ID)
                .add("redirect_uri", REDIRECT_URI)
                .add("grant_type", "authorization_code").build()

        val request = Request.Builder().header("Content-Type", "application/x-www-form-urlencoded")
            .url(url)
            .post(requestBody)
            .build()

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showToast(this@YouTubeChannelsActivity, "HJJJJJ")
                println()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val map: Map<*, *>? =
                        Gson().fromJson(response.body?.string(), Map::class.java)
                    getChannelId(map?.get("access_token") as String)
                } else {
                    //showToast(applicationContext, getString(R.string.chat_message_sending_failure_message))
                    println("$TAG: message sending failed")
                }
            }
        })
    }

    private fun getChannelId(accessToken: String) {
        val url = "https://www.googleapis.com/youtube/v3/channels?part=id,snippet&mine=true"
        val request = Request.Builder().header("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $accessToken")
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
                val channelId = (items[0] as LinkedTreeMap<*, *>)["id"] as String

                channelsRecyclerViewAdapter.insert(channelId)
                updateChannelId(channelId)
            } catch (e: Exception) {
                showToast(this, "채널을 읽어오는데 실패했습니다. (1)")
                println("$TAG: ${e.message}")
            }
        } else {
            showToast(this, "채널을 읽어오는데 실패했습니다. (2)")
            println("$TAG: failed to get response")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateChannelId(channelId: String) {
        user.channelIds.add(channelId)
        FirebaseFirestore.getInstance().collection(COLLECTION_USERS).document(user.id)
            .update(hashMapOf(UserModel.KEY_CHANNEL_IDS to user.channelIds) as HashMap<String, Any>)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    MainActivity.currentUser!!.registeredOnPartners = true
                    println("$TAG: \"channelIds\" updated")
                }
                else
                    println("$TAG: ${task.exception}")
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
                YouTubeVideosFragment(sourceId, allVideos, playlistTitle), YOUTUBE_VIDEOS_FRAGMENT_TAG).commit()
    }

    inner class ChannelsRecyclerViewAdapter(channelIds: MutableList<String>) : RecyclerView.Adapter<ChannelsRecyclerViewAdapter.ViewHolder>() {

        private val channels = mutableListOf<ChannelModel>()

        init {
            insertChannelsByIds(channelIds)
        }

        private fun insertChannelsByIds(channelIds: MutableList<String>) {
            val url = "https://www.googleapis.com/youtube/v3/channels?key=${getString(R.string.google_api_key)}&" +
                    "part=snippet,statistics&id=${channelIds.joinToString(separator = ",")}"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    showToast(this@YouTubeChannelsActivity, "비디오를 읽어오는데 실패했습니다.")
                    println("$TAG: failed to get video")
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
                                    item["snippet"] as LinkedTreeMap<*, *>
                                val title = snippet["title"] as String
                                val description = snippet["description"] as String
                                val thumbnailUri = ((snippet["thumbnails"]
                                        as LinkedTreeMap<*, *>)["default"]
                                        as LinkedTreeMap<*, *>)["url"] as String
                                channels.add(
                                    0,
                                    ChannelModel(id, description, thumbnailUri, title)
                                )

                                CoroutineScope(Dispatchers.Main).launch {
                                    notifyItemInserted(0)
                                }
                            }
                        } catch (e: Exception) {
                            showToast(this@YouTubeChannelsActivity, "채널을 읽어오는데 실패했습니다. (3)")
                            println("$TAG KINSTONE: ${e.message}")
                        }
                    } else {
                        showToast(this@YouTubeChannelsActivity, "비디오를 읽어오는데 실패했습니다. (4)")
                        println("$TAG: failed to get video")
                    }
                }

            })
        }

        private fun insertChannelById(channelId: String) {
            if (channelId in user.channelIds) {
                showToast(this@YouTubeChannelsActivity, "이미 등록된 채널입니다.")
                return
            }

            val url = "https://www.googleapis.com/youtube/v3/channels?key=${getString(R.string.google_api_key)}&" +
                    "part=snippet,statistics&id=$channelId"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    showToast(this@YouTubeChannelsActivity, "비디오를 읽어오는데 실패했습니다.")
                    println("$TAG: failed to get video")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            val map: Map<*, *>? =
                                Gson().fromJson(response.body?.string(), Map::class.java)
                            val snippet = ((map?.get("items") as ArrayList<*>)[0]
                                    as LinkedTreeMap<*, *>)["snippet"] as LinkedTreeMap<*, *>
                            val title = snippet["title"] as String
                            val description = snippet["description"] as String
                            val thumbnailUri = ((snippet["thumbnails"]
                                    as LinkedTreeMap<*, *>)["default"]
                                    as LinkedTreeMap<*, *>)["url"] as String

                            channels.add(0, ChannelModel(channelId, description, thumbnailUri, title))

                            CoroutineScope(Dispatchers.Main).launch {
                                notifyItemInserted(0)
                            }
                        } catch (e: Exception) {
                            showToast(this@YouTubeChannelsActivity, "채널을 읽어오는데 실패했습니다. (3)")
                            println("$TAG KINSTONE: ${e.message}")
                        }
                    } else {
                        showToast(this@YouTubeChannelsActivity, "비디오를 읽어오는데 실패했습니다. (4)")
                        println("$TAG: failed to get video")
                    }
                }

            })
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ChannelsRecyclerViewAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_view_channel, parent, false)

            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return channels.size
        }

        override fun onBindViewHolder(holder: ChannelsRecyclerViewAdapter.ViewHolder, position: Int) {
            val channel = channels[position]

            holder.view.text_view_title.text = channel.title
            holder.view.text_view_description
            loadImage(holder.view.image_view, channel.thumbnailUri)

            holder.view.setOnClickListener {
                PlaylistsDialogFragment(channel.id).show(this@YouTubeChannelsActivity.supportFragmentManager, TAG)
            }
        }

        private fun startPlaylistsFragment() {

        }

        private fun loadImage(imageView: ImageView, imageUri: String) {
            Glide.with(imageView.context)
                .load(imageUri)
                .error(R.drawable.ic_sentiment_dissatisfied_grey_24dp)
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(CenterCrop())
                .into(imageView)
        }

        private fun insert(channel: ChannelModel?) {
            if (channel != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    channels.add(0, channel)
                    notifyItemInserted(0)
                }
            }
        }

        fun insert(channelId: String) {
            insertChannelById(channelId)
        }

        fun update(pr: ChannelModel) {
            notifyItemChanged(getPosition(pr))
        }

        fun delete(channel: ChannelModel) {
            channels.remove(channel)
            notifyItemRemoved(getPosition(channel))
        }

        private fun getPosition(channel: ChannelModel) = channels.indexOf(channel)
    }

    companion object {
        private const val TAG = "YouTubeChannelsActivity"

        const val YOUTUBE_VIDEOS_FRAGMENT_TAG = "youtube_videos_fragment_tag"

        private const val ACTION_NEW_INTENT = "action_new_intent"
        private const val KEY_AUTHORIZATION_CODE = "key_authorization_code"

        private const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"

        private const val CLIENT_ID = "279682383751-8gp8j37rsri53neu6qgjc2lt80bouvjo.apps.googleusercontent.com"
        private const val REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob"
        private const val RESPONSE_TYPE = "code"
        private const val SCOPE = "https://www.googleapis.com/auth/youtube"
        // private const val YOUTUBE_SCOPE = "oauth2:https://www.googleapis.com/auth/youtube"

        const val GOOGLE_AUTHORIZATION_SERVER_URI = "https://accounts.google.com/o/oauth2/auth?" +
                "client_id=$CLIENT_ID&" +
                "redirect_uri=$REDIRECT_URI&" +
                "scope=$SCOPE&" +
                "response_type=$RESPONSE_TYPE&" +
                "access_type=offline"
    }
}
