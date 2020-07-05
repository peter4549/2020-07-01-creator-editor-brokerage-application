package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.widget.Toast
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.PhoneAuthFragment

class SmsReceiver : BroadcastReceiver() {

    private lateinit var onVerifyCodeListener: OnVerifyCodeListener

    override fun onReceive(context: Context?, intent: Intent?) {
        val bundle = intent?.extras!!
        val messages = parseSmsMessage(bundle)

        if (messages.isNotEmpty()) {
            // val originatingAddress = messages[0]?.originatingAddress
            val messageBody = messages[0]?.messageBody

            if (messageBody?.contains(SMS_TEMPLATE)!!) {
                val verificationCode = messageBody.filter { it.isDigit() }
                Toast.makeText(context, verificationCode, Toast.LENGTH_LONG).show()
                // 로그인된 계정에서 발송된 문자만이 유효합니다. 라는 메시지 필요.

                onVerifyCodeListener.onVerifyCode(verificationCode)

                if (PhoneAuthFragment.isSmsReceiverRegistered) {
                    context?.unregisterReceiver(this)
                    PhoneAuthFragment.isSmsReceiverRegistered = false
                }
            }
        }
    }

    interface OnVerifyCodeListener {
        fun onVerifyCode(code: String)
    }

    fun setListener(onVerifyCodeListener: OnVerifyCodeListener) {
        this.onVerifyCodeListener = onVerifyCodeListener
    }

    private fun parseSmsMessage(bundle: Bundle): Array<SmsMessage?> {
        val objects = bundle.get("pdus") as Array<*>
        val messages: Array<SmsMessage?> = arrayOfNulls(objects.size)

        for (i in objects.indices) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val format = bundle.getString("format")
                messages[i] = SmsMessage.createFromPdu(objects[i] as ByteArray, format)
            } else
                messages[i] = SmsMessage.createFromPdu(objects[i] as ByteArray)
        }

        return messages
    }

    companion object {
        const val SMS_TEMPLATE =  "is your verification code."
    }
}