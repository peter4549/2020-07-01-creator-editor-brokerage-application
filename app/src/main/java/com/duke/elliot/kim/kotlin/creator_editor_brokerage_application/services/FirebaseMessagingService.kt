package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService: com.google.firebase.messaging.FirebaseMessagingService() {

    private lateinit var roomId: String

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.isNotEmpty().let {
            roomId = remoteMessage.data["roomId"]!!

            val message = remoteMessage.data["message"] ?: "Message Error"
            val senderPublicName = remoteMessage.data["senderPublicName"] ?: "Sender Error"

            if (MainActivity.currentChatRoomId != roomId)
                sendNotification(senderPublicName, message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("$TAG: new token: $token")
    }

    private fun sendNotification(senderPublicName: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.action = ACTION_CHAT_NOTIFICATION
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(KEY_CHAT_ROOM_ID, roomId)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setContentText(message)
            .setContentTitle(senderPublicName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_star_64dp)
            .setSound(defaultSoundUri)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                CHANNEL_TITLE,
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "FirebaseMessagingService"

        const val ACTION_CHAT_NOTIFICATION = "action_chat_notification"
        const val KEY_CHAT_ROOM_ID = "key_chat_room_id"
        private const val CHANNEL_TITLE = "channel title"
    }
}