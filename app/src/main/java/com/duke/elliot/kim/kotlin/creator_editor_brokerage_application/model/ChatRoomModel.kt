package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.*

@Suppress("UNCHECKED_CAST")
class ChatRoomModel(map: Map<String, Any>? = null) {

    var roomId: String
    var memberIds : MutableList<String?> = mutableListOf()
    var creationTime: String
    var publisherId: String
    var latestMessage: String
    var memberPublicNames: MutableList<String> = mutableListOf()

    init {
        if (map == null) {
            roomId = ""
            creationTime = ""
            publisherId = ""
            latestMessage = ""
        } else {
            roomId = map[KEY_CHAT_ROOM_ID] as String
            creationTime = map[KEY_CHAT_ROOM_CREATION_TIME] as String
            publisherId = map[KEY_CHAT_ROOM_PUBLISHER_ID] as String
            latestMessage = map[KEY_CHAT_ROOM_LATEST_MESSAGE] as String
            memberIds = map[KEY_CHAT_ROOM_MEMBER_IDS] as MutableList<String?>
            memberPublicNames = map[KEY_CHAT_ROOM_MEMBER_PUBLIC_NAMES] as MutableList<String>
        }
    }
}