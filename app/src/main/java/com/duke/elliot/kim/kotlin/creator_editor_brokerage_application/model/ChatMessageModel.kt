package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.CHAT_MASSAGE
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.CHAT_PUBLIC_NAME
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.CHAT_TIME

class ChatMessageModel(map: Map<String, Any>? = null) {
    var publicName: String? = null
    var message: String
    var time: String

    init {
        if (map == null) {
            message = ""
            time = ""
        } else {
            publicName = map[CHAT_PUBLIC_NAME] as String
            message = map[CHAT_MASSAGE] as String
            time = map[CHAT_TIME] as String
        }
    }

}