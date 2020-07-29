package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast

class CustomTabsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val url = intent?.dataString
        showToast(context!!, url!!)
        println("CALLLOFDUU $url") // 맨뒤에 approval Code가 인증 토큰이다.
        if (url.startsWith("https://accounts.google.com/o/oauth2")) {
            val approvalCodeIntent = Intent(context, MainActivity::class.java)
            approvalCodeIntent.action = ACTION_APPROVAL_CODE
            approvalCodeIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP) // 이게 되면 제일 스고이인데..
            //approvalCodeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // 잘모르겟는데 이거 ㅋㅋ ㅅㅂ new task 플래그.. 이게 아니랴 걍 돌아가야되는데.
            context.startActivity(approvalCodeIntent)
        }
    }

    companion object {
        const val ACTION_APPROVAL_CODE = "action_approval_code"
    }
}