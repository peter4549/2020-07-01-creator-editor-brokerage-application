package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

class NotificationModel {

    var to: String = ""
    var notification: Notification = Notification()

    class Notification {
        var title: String = ""
        var text: String = ""
    }

}