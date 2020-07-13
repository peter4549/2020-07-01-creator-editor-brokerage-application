package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {

    private lateinit var onSetCodeListener: OnSetCodeListener

    override fun onReceive(context: Context?, intent: Intent?) {
        val bundle = intent?.extras!!
        val messages = parseSmsMessage(bundle)

        if (messages.isNotEmpty()) {
            // val originatingAddress = messages[0]?.originatingAddress
            val messageBody = messages[0]?.messageBody

            if (messageBody?.contains(SMS_TEMPLATE)!!) {
                val verificationCode = messageBody.filter { it.isDigit() }
                onSetCodeListener.onSetCode(verificationCode)
            }
        }
    }

    interface OnSetCodeListener {
        fun onSetCode(code: String)
    }

    fun setListener(onSetCodeListener: OnSetCodeListener) {
        this.onSetCodeListener = onSetCodeListener
    }

    private fun parseSmsMessage(bundle: Bundle): Array<SmsMessage?> {
        val objects = bundle.get("pdus") as Array<*>
        val messages: Array<SmsMessage?> = arrayOfNulls(objects.size)

        for (i in objects.indices) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val format = bundle.getString("format")
                messages[i] = SmsMessage.createFromPdu(objects[i] as ByteArray, format)
            } else {
                @Suppress("DEPRECATION")
                messages[i] = SmsMessage.createFromPdu(objects[i] as ByteArray)
            }
        }

        return messages
    }

    companion object {
        const val SMS_TEMPLATE =  "is your verification code."
    }
}