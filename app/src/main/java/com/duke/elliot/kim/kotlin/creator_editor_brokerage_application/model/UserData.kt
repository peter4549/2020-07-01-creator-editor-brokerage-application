package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.*

class UserData(var data: Map<String, Any>) {
    var name: String = data[NAME] as String
    var publicName: String = data[PUBLIC_NAME] as String
    var isVerified: Boolean = data[IS_VERIFIED] as Boolean
    var phoneNumber: String = data[PHONE_NUMBER] as String
    var age: String = data[AGE] as String
    var gender: Int = (data[GENDER] as Long).toInt()
    var pr: String = data[PR] as String

    fun updateData(data: Map<String, Any>) {
        name = data[NAME] as String
        publicName = data[PUBLIC_NAME] as String
        isVerified = data[IS_VERIFIED] as Boolean
        phoneNumber = data[PHONE_NUMBER] as String
        age = data[AGE] as String
        gender = (data[GENDER] as Long).toInt()
        pr = data[PR] as String
    }
}