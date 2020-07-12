package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.KEY_CHAT_ROOM_CREATION_TIME
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.KEY_CHAT_ROOM_ID
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.KEY_CHAT_ROOM_MEMBER_IDS
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.KEY_CHAT_ROOM_PUBLISHER_ID

class ChatRoomModel(map: Map<String, Any>? = null) {

    var roomId: String
    var memberIds : MutableList<String?> = mutableListOf()
    var creationTime: String
    var publisherId: String

    init {
        if (map == null) {
            roomId = ""
            creationTime = ""
            publisherId = ""
        } else {
            roomId = map[KEY_CHAT_ROOM_ID] as String
            @Suppress("UNCHECKED_CAST")
            memberIds = map[KEY_CHAT_ROOM_MEMBER_IDS] as MutableList<String?>
            creationTime = map[KEY_CHAT_ROOM_CREATION_TIME] as String
            publisherId = map[KEY_CHAT_ROOM_PUBLISHER_ID] as String
        }
    }
}