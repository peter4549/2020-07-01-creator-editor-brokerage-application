package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

@Suppress("UNCHECKED_CAST")
class PartnerModel(map: Map<String, Any>? = null) {

    var occupation: String
    var profileImageUri: String
    var publicName: String
    var stars: Int
    var statusMessage: String
    var uid: String

    init {
        if (map == null) {
            occupation = ""
            profileImageUri = ""
            publicName = ""
            stars = 0
            statusMessage = ""
            uid = ""
        } else {
            occupation = map[KEY_OCCUPATION] as String
            profileImageUri = map[KEY_PROFILE_IMAGE_URI] as String
            publicName = map[KEY_PUBLIC_NAME] as String
            stars = (map[KEY_STARS] as Long).toInt()
            statusMessage = map[KEY_STATUS_MESSAGE] as String
            uid = map[KEY_UID] as String
        }
    }

    fun toHashMap(): HashMap<String, Any> =
        hashMapOf(
            KEY_OCCUPATION to occupation,
            KEY_PROFILE_IMAGE_URI to profileImageUri,
            KEY_PUBLIC_NAME to publicName,
            KEY_STARS to stars,
            KEY_UID to uid
        )

    companion object {
        const val KEY_OCCUPATION = "occupation"
        const val KEY_PROFILE_IMAGE_URI = "profileImageUri"
        const val KEY_PUBLIC_NAME = "publicName"
        const val KEY_STARS = "stars"
        const val KEY_STATUS_MESSAGE = "statusMessage"
        const val KEY_UID = "uid"
    }
}