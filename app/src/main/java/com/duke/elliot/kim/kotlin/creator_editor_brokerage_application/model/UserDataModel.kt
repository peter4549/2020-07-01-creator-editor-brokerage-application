package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.*

class UserDataModel(map: Map<String, Any>? = null) {

    var userId: String = ""
    var name: String = ""
    var publicName: String = ""
    var isVerified: Boolean = false
    var phoneNumber: String = ""
    var age: Int = 0
    var gender: Int = 0
    var pr: String = ""
    var myChatRooms : MutableList<String?>
    var pushToken: String?

    init {
        if (map == null) {
            userId = ""
            name = ""
            publicName = ""
            isVerified = false
            phoneNumber = ""
            age = 0
            gender = 0
            pr = ""
            myChatRooms = mutableListOf()
            pushToken = null
        } else {
            name = map[KEY_USER_NAME] as String
            userId = map[KEY_USER_ID] as String
            publicName = map[KEY_USER_PUBLIC_NAME] as String
            isVerified = map[KEY_USER_VERIFIED] as Boolean
            phoneNumber = map[KEY_USER_PHONE_NUMBER] as String
            age = (map[KEY_USER_AGE] as Long).toInt()
            gender = (map[KEY_USER_GENDER] as Long).toInt()
            pr = map[KEY_USER_PR] as String
            @Suppress("UNCHECKED_CAST")
            myChatRooms = map[KEY_USER_CHAT_ROOMS] as MutableList<String?>
            pushToken = map[KEY_USER_PUSH_TOKEN] as String?
        }
    }

    fun setData(map: Map<String, Any>) {
        name = map[KEY_USER_NAME] as String
        userId = map[KEY_USER_ID] as String
        publicName = map[KEY_USER_PUBLIC_NAME] as String
        isVerified = map[KEY_USER_VERIFIED] as Boolean
        phoneNumber = map[KEY_USER_PHONE_NUMBER] as String
        age = (map[KEY_USER_AGE] as Long).toInt()
        gender = (map[KEY_USER_GENDER] as Long).toInt()
        pr = map[KEY_USER_PR] as String
        @Suppress("UNCHECKED_CAST")
        myChatRooms = map[KEY_USER_CHAT_ROOMS] as MutableList<String?>
        pushToken = map[KEY_USER_PUSH_TOKEN] as String?
    }

    fun clear() {
        name = ""
        publicName = ""
        isVerified = false
        phoneNumber = ""
        age = 0
        gender = 0
        pr = ""
        myChatRooms = mutableListOf()
        pushToken = null
    }
}