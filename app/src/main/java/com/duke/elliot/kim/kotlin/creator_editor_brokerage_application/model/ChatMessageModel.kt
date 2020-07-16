package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

class ChatMessageModel(map: Map<String, Any>? = null) {

    var message: String
    var senderName: String
    var time: String

    init {
        if (map == null) {
            message = ""
            senderName = ""
            time = ""
        } else {
            message = map[KEY_MESSAGE] as String
            senderName = map[KEY_SENDER_NAME] as String
            time = map[KEY_TIME] as String
        }
    }

    companion object {
        const val KEY_MESSAGE = "message"
        const val KEY_SENDER_NAME = "senderName"
        const val KEY_TIME = "time"
    }
}