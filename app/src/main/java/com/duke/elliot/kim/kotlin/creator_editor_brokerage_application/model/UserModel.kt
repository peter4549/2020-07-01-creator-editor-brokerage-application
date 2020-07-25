package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

@Suppress("UNCHECKED_CAST")
class UserModel(map: Map<String, Any>? = null) {

    var comments: HashMap<String, String>
    var id: String
    var name: String
    var occupation: String
    var phoneNumber: String
    var pr: String
    var profileImageFileDownloadUri: String
    var publicName: String
    var pushToken: String?
    var registeredOnPartners: Boolean
    var stars: MutableList<String>
    var verified: Boolean

    init {
        if (map == null) {
            comments = hashMapOf()
            id = ""
            name = ""
            occupation = ""
            phoneNumber = ""
            pr = ""
            profileImageFileDownloadUri = ""
            publicName = ""
            pushToken = null
            registeredOnPartners = false
            stars = mutableListOf()
            verified = false
        } else {
            comments = map[KEY_COMMENTS] as HashMap<String, String>
            id = map[KEY_ID] as String
            name = map[KEY_NAME] as String
            occupation = map[KEY_OCCUPATION] as String
            phoneNumber = map[KEY_PHONE_NUMBER] as String
            pr = map[KEY_PR] as String
            profileImageFileDownloadUri = map[KEY_PROFILE_IMAGE_FILE_DOWNLOAD_URI] as String
            publicName = map[KEY_PUBLIC_NAME] as String
            pushToken = map[KEY_PUSH_TOKEN] as String?
            registeredOnPartners = map[KEY_REGISTERED_ON_PARTNERS] as Boolean
            stars = map[KEY_STARS] as MutableList<String>
            verified = map[KEY_VERIFIED] as Boolean
        }
    }

    fun setData(map: Map<String, Any>) {
        comments = map[KEY_COMMENTS] as HashMap<String, String>
        id = map[KEY_ID] as String
        name = map[KEY_NAME] as String
        occupation = map[KEY_OCCUPATION] as String
        phoneNumber = map[KEY_PHONE_NUMBER] as String
        pr = map[KEY_PR] as String
        profileImageFileDownloadUri = map[KEY_PROFILE_IMAGE_FILE_DOWNLOAD_URI] as String
        publicName = map[KEY_PUBLIC_NAME] as String
        pushToken = map[KEY_PUSH_TOKEN] as String?
        registeredOnPartners = map[KEY_REGISTERED_ON_PARTNERS] as Boolean
        stars = map[KEY_STARS] as MutableList<String>
        verified = map[KEY_VERIFIED] as Boolean
    }

    fun toHashMap(): HashMap<String, Any?> =
        hashMapOf(
            KEY_COMMENTS to comments,
            KEY_ID to id,
            KEY_NAME to name,
            KEY_OCCUPATION to occupation,
            KEY_PHONE_NUMBER to phoneNumber,
            KEY_PR to pr,
            KEY_PROFILE_IMAGE_FILE_DOWNLOAD_URI to profileImageFileDownloadUri,
            KEY_PUBLIC_NAME to publicName,
            KEY_PUSH_TOKEN to pushToken,
            KEY_REGISTERED_ON_PARTNERS to registeredOnPartners,
            KEY_STARS to stars,
            KEY_VERIFIED to verified
        )

    companion object {
        const val KEY_COMMENTS = "comments"
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_OCCUPATION = "occupation"
        const val KEY_PHONE_NUMBER = "phoneNumber"
        const val KEY_PR = "pr"
        const val KEY_PROFILE_IMAGE_FILE_DOWNLOAD_URI = "profileImageFileDownloadUri"
        const val KEY_PUBLIC_NAME = "publicName"
        const val KEY_PUSH_TOKEN = "pushToken"
        const val KEY_REGISTERED_ON_PARTNERS = "registeredOnPartners"
        const val KEY_STARS = "stars"
        const val KEY_VERIFIED = "verified"
    }
}