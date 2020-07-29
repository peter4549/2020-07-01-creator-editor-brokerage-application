package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class CheckUrlJobIntentService: Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val url = intent?.dataString ?: ""
        if (url.startsWith("https://accounts.google.com/o/oauth2")) {
            val code = intent?.data?.getQueryParameter("approvalCode")
            sendBroadcast(code!!)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    private fun sendBroadcast(code: String) {
        val intent = Intent(ACTION_CORRECT_URL)
        intent.putExtra(KEY_AUTHORIZATION_CODE, code)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        const val ACTION_CORRECT_URL = "action_correct_url"
        const val KEY_AUTHORIZATION_CODE = "key_authorization_code"
    }
}