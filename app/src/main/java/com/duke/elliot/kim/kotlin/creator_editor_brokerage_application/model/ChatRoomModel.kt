package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

@Suppress("UNCHECKED_CAST")
class ChatRoomModel(map: Map<String, Any>? = null) {

    var creationTime: String
    var groupNotificationKey: String
    var lastMessage: String
    var publisherId: String
    var publisherName: String
    var roomId: String
    var userIds : MutableList<String> = mutableListOf()
    var userPublicNames: MutableList<String> = mutableListOf()
    var userPushTokens: MutableList<String> = mutableListOf()

    init {
        if (map == null) {
            creationTime = ""
            groupNotificationKey = ""
            lastMessage = ""
            roomId = ""
            publisherId = ""
            publisherName = ""
        } else {
            creationTime = map[KEY_CREATION_TIME] as String
            groupNotificationKey = map[KEY_GROUP_NOTIFICATION_KEY] as String
            lastMessage = map[KEY_LAST_MESSAGE] as String
            publisherId = map[KEY_PUBLISHER_ID] as String
            publisherName = map[KEY_PUBLISHER_NAME] as String
            roomId = map[KEY_ROOM_ID] as String
            userIds = map[KEY_USER_IDS] as MutableList<String>
            userPublicNames = map[KEY_USER_PUBLIC_NAMES] as MutableList<String>
            userPushTokens = map[KEY_USER_PUSH_TOKENS] as MutableList<String>
        }
    }

    companion object {
        const val KEY_CREATION_TIME = "creationTime"
        const val KEY_GROUP_NOTIFICATION_KEY = "groupNotificationKey"
        const val KEY_LAST_MESSAGE = "lastMessage"
        const val KEY_PUBLISHER_ID = "publisherId"
        const val KEY_PUBLISHER_NAME = "publisherName"
        const val KEY_ROOM_ID = "roomId"
        const val KEY_USER_IDS = "userIds"
        const val KEY_USER_PUBLIC_NAMES = "userPublicNames"
        const val KEY_USER_PUSH_TOKENS = "userPushTokens"
    }
}