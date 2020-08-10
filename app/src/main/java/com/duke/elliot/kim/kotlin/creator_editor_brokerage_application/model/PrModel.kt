package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

@Suppress("UNCHECKED_CAST")
class PrModel(map: Map<String, Any>? = null) {

    var categories: MutableList<String?>
    var content: String
    var occupation: String
    var publisherId: String
    var publisherName: String
    var registrationTime: String
    var title: String
    var youtubeVideos: MutableList<HashMap<*, *>?>

    init {
        if (map == null) {
            categories = mutableListOf()
            content = ""
            occupation = ""
            publisherId = ""
            publisherName = ""
            registrationTime = ""
            title = ""
            youtubeVideos = mutableListOf(null, null, null)
        } else {
            categories = map[KEY_CATEGORIES] as MutableList<String?>
            content = map[KEY_CONTENT] as String
            occupation = map[KEY_OCCUPATION] as String
            publisherId = map[KEY_PUBLISHER_ID] as String
            publisherName = map[KEY_PUBLISHER_NAME] as String
            registrationTime = map[KEY_REGISTRATION_TIME] as String
            title = map[KEY_TITLE] as String
            youtubeVideos = map[KEY_YOUTUBE_VIDEOS] as MutableList<HashMap<*, *>?>
        }
    }

    companion object {
        const val KEY_CATEGORIES = "categories"
        const val KEY_CONTENT = "content"
        const val KEY_OCCUPATION = "occupation"
        const val KEY_PUBLISHER_ID = "publisherId"
        const val KEY_PUBLISHER_NAME = "publisherName"
        const val KEY_REGISTRATION_TIME = "registrationTime"
        const val KEY_TITLE = "title"
        const val KEY_YOUTUBE_VIDEOS = "youtubeVideos"
    }
}