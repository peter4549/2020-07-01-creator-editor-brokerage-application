package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

import java.io.Serializable

class VideoModel (var channelId: String,
                  var description: String,
                  var publishTime: String,
                  var thumbnailUri: String,
                  var title: String,
                  var videoId: String) : Serializable {

    constructor(hashMap: HashMap<*, *>) : this(
        hashMap[KEY_CHANNEL_ID] as String,
        hashMap[KEY_DESCRIPTION] as String,
        hashMap[KEY_PUBLISH_TIME] as String,
        hashMap[KEY_THUMBNAIL_URI] as String,
        hashMap[KEY_TITLE] as String,
        hashMap[KEY_VIDEO_ID] as String
    )

    fun toHashMap(): HashMap<*, *> =
        hashMapOf(
            KEY_CHANNEL_ID to channelId,
            KEY_DESCRIPTION to description,
            KEY_PUBLISH_TIME to publishTime,
            KEY_THUMBNAIL_URI to thumbnailUri,
            KEY_TITLE to title,
            KEY_VIDEO_ID to videoId
        )


    companion object {
        const val KEY_CHANNEL_ID = "channelId"
        const val KEY_DESCRIPTION = "description"
        const val KEY_PUBLISH_TIME = "publishTime"
        const val KEY_THUMBNAIL_URI = "thumbnailUri"
        const val KEY_TITLE = "title"
        const val KEY_VIDEO_ID = "videoId"
    }
}

fun HashMap<*, *>.toVideoModel(): VideoModel =
    VideoModel(
        this[VideoModel.KEY_CHANNEL_ID] as String,
        this[VideoModel.KEY_DESCRIPTION] as String,
        this[VideoModel.KEY_PUBLISH_TIME] as String,
        this[VideoModel.KEY_THUMBNAIL_URI] as String,
        this[VideoModel.KEY_TITLE] as String,
        this[VideoModel.KEY_VIDEO_ID] as String
    )