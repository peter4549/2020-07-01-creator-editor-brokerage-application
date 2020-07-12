package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.*

class PRModel(map: Map<String, Any>? = null) {
    var userId: String
    var publisher: String
    var occupation: String
    var category: String
    var title: String
    var content: String
    var imageNames: MutableList<String?>
    var registrationTime: String = ""
    var pushToken: String? = null

    init {
        if (map == null) {
            userId = ""
            publisher = ""
            occupation = ""
            category = ""
            title = ""
            content = ""
            imageNames = mutableListOf(null, null, null)
            registrationTime = ""
        } else {
            userId = map[USER_ID] as String
            publisher = map[PUBLISHER] as String
            occupation = map[OCCUPATION] as String
            category = map[CATEGORY] as String
            title = map[TITLE] as String
            content = map[CONTENT] as String
            @Suppress("UNCHECKED_CAST")
            imageNames = map[IMAGE_NAMES] as MutableList<String?>
            registrationTime = map[REGISTRATION_TIME] as String
            pushToken = map[KEY_PUSH_TOKEN] as String?
        }
    }

    fun setData(map: Map<String, Any>) {
        userId = map[USER_ID] as String
        publisher = map[PUBLISHER] as String
        occupation = map[OCCUPATION] as String
        category = map[CATEGORY] as String
        title = map[TITLE] as String
        content = map[CONTENT] as String
        @Suppress("UNCHECKED_CAST")
        imageNames = map[IMAGE_NAMES] as MutableList<String?>
        registrationTime = map[REGISTRATION_TIME] as String
        pushToken = map[KEY_PUSH_TOKEN] as String?
    }

}