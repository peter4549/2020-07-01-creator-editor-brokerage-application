package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

@Suppress("PropertyName")
class GroupNotificationModel {
    var operation = "create"
    var notification_key_name = ""
    var registration_ids: MutableList<String> = mutableListOf()
}