package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model

class CloudMessageModel {

    var to: String = ""
    var notification: Notification = Notification()
    var data: Data = Data()

    class Data {
        var roomId: String = ""
        var senderPublicName: String = ""
        var message: String = ""
    }

    class Notification {
        var title: String = ""
        var text: String = ""
    }
}