package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

@Suppress("UNCHECKED_CAST")
class PrModel(map: Map<String, Any>? = null) {

    var category: String
    var content: String
    var imageNames: MutableList<String?>
    var occupation: String
    var publisherId: String
    var publisherName: String
    var registrationTime: String = ""
    var title: String

    init {
        if (map == null) {
            category = ""
            content = ""
            imageNames = mutableListOf(null, null, null)
            occupation = ""
            publisherId = ""
            publisherName = ""
            registrationTime = ""
            title = ""
        } else {
            category = map[KEY_CATEGORY] as String
            content = map[KEY_CONTENT] as String
            imageNames = map[KEY_IMAGE_NAMES] as MutableList<String?>
            occupation = map[KEY_OCCUPATION] as String
            publisherId = map[KEY_PUBLISHER_ID] as String
            publisherName = map[KEY_PUBLISHER_NAME] as String
            registrationTime = map[KEY_REGISTRATION_TIME] as String
            title = map[KEY_TITLE] as String
        }
    }

    fun setData(map: Map<String, Any>) {
        category = map[KEY_CATEGORY] as String
        content = map[KEY_CONTENT] as String
        imageNames = map[KEY_IMAGE_NAMES] as MutableList<String?>
        occupation = map[KEY_OCCUPATION] as String
        publisherId = map[KEY_PUBLISHER_ID] as String
        publisherName = map[KEY_PUBLISHER_NAME] as String
        registrationTime = map[KEY_REGISTRATION_TIME] as String
        title = map[KEY_TITLE] as String
    }

    companion object {
        const val KEY_CATEGORY = "category"
        const val KEY_CONTENT = "content"
        const val KEY_IMAGE_NAMES = "imageNames"
        const val KEY_OCCUPATION = "occupation"
        const val KEY_PUBLISHER_ID = "publisherId"
        const val KEY_PUBLISHER_NAME = "publisherName"
        const val KEY_REGISTRATION_TIME = "registrationTime"
        const val KEY_TITLE = "title"
    }
}