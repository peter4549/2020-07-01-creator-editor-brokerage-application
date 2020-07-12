package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.KEY_CHAT_MESSAGE
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.KEY_CHAT_PUBLIC_NAME
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.KEY_CHAT_TIME

class ChatMessageModel(map: Map<String, Any>? = null) {
    var publicName: String? = null
    var message: String
    var time: String

    init {
        if (map == null) {
            message = ""
            time = ""
        } else {
            publicName = map[KEY_CHAT_PUBLIC_NAME] as String?
            message = map[KEY_CHAT_MESSAGE] as String
            time = map[KEY_CHAT_TIME] as String
        }
    }

}