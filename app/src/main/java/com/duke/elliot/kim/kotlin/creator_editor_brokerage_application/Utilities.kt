package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application

import java.security.MessageDigest

fun hashString(input: String, algorithm: String = "SHA-256"): String {
    return MessageDigest.getInstance(algorithm)
        .digest(input.toByteArray())
        .fold("", { string, it -> string + "%02x".format(it) })
}