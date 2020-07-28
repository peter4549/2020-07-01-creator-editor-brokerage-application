package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast

class CustomTabsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val code = intent?.dataString
        showToast(context!!, code!!)
        println("CALLLOFDUU $code") // 맨뒤에 approval Code가 인증 토큰이다.
    }
}