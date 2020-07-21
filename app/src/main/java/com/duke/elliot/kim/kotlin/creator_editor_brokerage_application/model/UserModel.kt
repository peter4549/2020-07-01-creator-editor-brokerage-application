package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

class UserModel(map: Map<String, Any>? = null) {

    var id: String
    var name: String
    var occupation: String
    var phoneNumber: String
    var pr: String
    var profileImageFileDownloadUri: String
    var publicName: String
    var pushToken: String?
    var verified: Boolean

    init {
        if (map == null) {
            name = ""
            occupation = ""
            phoneNumber = ""
            pr = ""
            profileImageFileDownloadUri = ""
            publicName = ""
            pushToken = null
            id = ""
            verified = false
        } else {
            name = map[KEY_NAME] as String
            occupation = map[KEY_OCCUPATION] as String
            phoneNumber = map[KEY_PHONE_NUMBER] as String
            pr = map[KEY_PR] as String
            profileImageFileDownloadUri = map[KEY_PROFILE_IMAGE_FILE_DOWNLOAD_URI] as String
            publicName = map[KEY_PUBLIC_NAME] as String
            pushToken = map[KEY_PUSH_TOKEN] as String?
            id = map[KEY_ID] as String
            verified = map[KEY_VERIFIED] as Boolean
        }
    }

    fun setData(map: Map<String, Any>) {
        name = map[KEY_NAME] as String
        occupation = map[KEY_OCCUPATION] as String
        phoneNumber = map[KEY_PHONE_NUMBER] as String
        pr = map[KEY_PR] as String
        profileImageFileDownloadUri = map[KEY_PROFILE_IMAGE_FILE_DOWNLOAD_URI] as String
        publicName = map[KEY_PUBLIC_NAME] as String
        pushToken = map[KEY_PUSH_TOKEN] as String?
        id = map[KEY_ID] as String
        verified = map[KEY_VERIFIED] as Boolean
    }

    fun clear() {
        name = ""
        phoneNumber = ""
        pr = ""
        publicName = ""
        pushToken = null
        verified = false
    }

    companion object {
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_OCCUPATION = "occupation"
        const val KEY_PHONE_NUMBER = "phoneNumber"
        const val KEY_PR = "pr"
        const val KEY_PROFILE_IMAGE_FILE_DOWNLOAD_URI = "profileImageFileDownloadUri"
        const val KEY_PUBLIC_NAME = "publicName"
        const val KEY_PUSH_TOKEN = "pushToken"
        const val KEY_VERIFIED = "verified"
    }
}