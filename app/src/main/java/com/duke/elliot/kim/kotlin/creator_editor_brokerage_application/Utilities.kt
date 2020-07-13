package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

fun hashString(input: String, algorithm: String = "SHA-256"): String {
    return MessageDigest.getInstance(algorithm)
        .digest(input.toByteArray())
        .fold("", { string, it -> string + "%02x".format(it) })
}

fun showToast(context: Context, text: String, duration: Int = Toast.LENGTH_SHORT) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(context, text, duration).show()
    }
}