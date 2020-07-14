package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.*

class PrModel(map: Map<String, Any>? = null) {
    var userId: String
    var publisherName: String
    var occupation: String
    var category: String
    var title: String
    var content: String
    var imageNames: MutableList<String?>
    var registrationTime: String = ""

    init {
        if (map == null) {
            userId = ""
            publisherName = ""
            occupation = ""
            category = ""
            title = ""
            content = ""
            imageNames = mutableListOf(null, null, null)
            registrationTime = ""
        } else {
            userId = map[KEY_PR_USER_ID] as String
            publisherName = map[KEY_PR_PUBLISHER_NAME] as String
            occupation = map[KEY_PR_OCCUPATION] as String
            category = map[KEY_PR_CATEGORY] as String
            title = map[KEY_PR_TITLE] as String
            content = map[KEY_PR_CONTENT] as String
            @Suppress("UNCHECKED_CAST")
            imageNames = map[KEY_PR_IMAGE_NAMES] as MutableList<String?>
            registrationTime = map[KEY_PR_REGISTRATION_TIME] as String
        }
    }

    fun setData(map: Map<String, Any>) {
        userId = map[KEY_PR_USER_ID] as String
        publisherName = map[KEY_PR_PUBLISHER_NAME] as String
        occupation = map[KEY_PR_OCCUPATION] as String
        category = map[KEY_PR_CATEGORY] as String
        title = map[KEY_PR_TITLE] as String
        content = map[KEY_PR_CONTENT] as String
        @Suppress("UNCHECKED_CAST")
        imageNames = map[KEY_PR_IMAGE_NAMES] as MutableList<String?>
        registrationTime = map[KEY_PR_REGISTRATION_TIME] as String
    }

}