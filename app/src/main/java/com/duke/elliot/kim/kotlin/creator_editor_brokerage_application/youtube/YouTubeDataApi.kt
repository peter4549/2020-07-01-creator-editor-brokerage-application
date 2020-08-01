package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.youtube

import android.content.Context
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.ChannelModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.PlaylistModel
import com.google.gson.internal.LinkedTreeMap
import okhttp3.FormBody
import okhttp3.Request

class YouTubeDataApi(context: Context) {

    private val googleApiKey = context.getString(R.string.google_api_key)

    fun getAuthorizationRequest(code: String) = Request.Builder()
        .header("Content-Type", "application/x-www-form-urlencoded")
        .url(GOOGLE_AUTHORIZATION_SERVER_URL)
        .post(getAuthorizationFormBody(code))
        .build()

    fun getGoogleAuthorizationServerUrlWithParameters() = "https://accounts.google.com/o/oauth2/auth?" +
            "client_id=$ANDROID_CLIENT_ID&" +
            "redirect_uri=$REDIRECT_URI&" +
            "scope=$SCOPE&" +
            "response_type=$RESPONSE_TYPE&" +
            "access_type=offline"

    private fun getAuthorizationFormBody(code: String) =
        FormBody.Builder().add("code", code)
            .add("client_id", ANDROID_CLIENT_ID)
            .add("redirect_uri", REDIRECT_URI)
            .add("grant_type", "authorization_code").build()

    fun getChannelIdsRequestByAccessToken(accessToken: String) =
        Request.Builder().header("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $accessToken")
            .url(CHANNELS_REQUEST_URL)
            .get()
            .build()

    fun getChannelsRequestByIds(channelIds: MutableList<String>): Request {
        //channelIds.joinToString(separator = ",")
        val url =
            "https://www.googleapis.com/youtube/v3/channels?key=$googleApiKey&" +
                    "part=snippet,statistics&id=${"aaaa,UCRM6LLEhw1kTUJs4iEan5NQ"}" // for make error
        return Request.Builder()
            .url(url)
            .get()
            .build()
    }

    fun getPlaylistsRequestByChannelId(channelId: String): Request {
        val url = "https://www.googleapis.com/youtube/v3/playlists?key=$googleApiKey&" +
                "part=snippet&channelId=$channelId"
        return Request.Builder()
            .url(url)
            .get()
            .build()
    }

    fun getPlaylistByItem(item: LinkedTreeMap<*, *>): PlaylistModel {
        val id = item["id"] as String
        val snippet = item["snippet"] as LinkedTreeMap<*, *>
        val title = snippet["title"] as String
        val description = snippet["description"] as String
        val thumbnailUri = ((snippet["thumbnails"]
                as LinkedTreeMap<*, *>)["default"]
                as LinkedTreeMap<*, *>)["url"] as String
        return PlaylistModel(description, id, thumbnailUri, title)
    }

    fun getChannelRequestById(channelId: String): Request {
        val url = "https://www.googleapis.com/youtube/v3/channels?key=$googleApiKey&" +
                "part=snippet,statistics&id=$channelId"
        return Request.Builder()
            .url(url)
            .get()
            .build()
    }

    fun getItemsFromMap(map: Map<*, *>) = map["items"] as ArrayList<*>

    fun getChannelByItem(item: LinkedTreeMap<*, *>): ChannelModel {
        val id = item["id"] as String
        val snippet = item["snippet"] as LinkedTreeMap<*, *>
        val title = snippet["title"] as String
        val description = snippet["description"] as String
        val thumbnailUri = ((snippet["thumbnails"]
                as LinkedTreeMap<*, *>)["default"]
                as LinkedTreeMap<*, *>)["url"] as String
        return ChannelModel(id, description, thumbnailUri, title)
    }

    companion object {
        private const val GOOGLE_AUTHORIZATION_SERVER_URL = "https://accounts.google.com/o/oauth2/token"
        private const val ANDROID_CLIENT_ID = "279682383751-8gp8j37rsri53neu6qgjc2lt80bouvjo.apps.googleusercontent.com"
        private const val CHANNELS_REQUEST_URL = "https://www.googleapis.com/youtube/v3/channels?part=id&mine=true"
        private const val REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob"
        private const val RESPONSE_TYPE = "code"
        private const val SCOPE = "https://www.googleapis.com/auth/youtube"
        // private const val YOUTUBE_OAUTH_SCOPE = "oauth2:https://www.googleapis.com/auth/youtube"
    }
}