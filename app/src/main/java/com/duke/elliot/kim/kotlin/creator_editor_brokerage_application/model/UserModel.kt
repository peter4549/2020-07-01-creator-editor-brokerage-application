package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.*

class UserModel {

    var name: String = ""
    var publicName: String = ""
    var isVerified: Boolean = false
    var phoneNumber: String = ""
    var age: Int = 0
    var gender: Int = 0
    var pr: String = ""

    fun setData(map: Map<String, Any>) {
        name = map[NAME] as String
        publicName = map[PUBLIC_NAME] as String
        isVerified = map[IS_VERIFIED] as Boolean
        phoneNumber = map[PHONE_NUMBER] as String
        age = (map[AGE] as Long).toInt()
        gender = (map[GENDER] as Long).toInt()
        pr = map[PR] as String
    }

    fun finalize() {  }
}