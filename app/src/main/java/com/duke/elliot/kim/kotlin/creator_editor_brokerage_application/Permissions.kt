package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

val RECEIVE_SMS_PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.RECEIVE_SMS)
val READ_EXTERNAL_STORAGE_REQUIRED = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

fun getPermissionsRequired(context: Context): Array<String> {
    var permissionsRequired = arrayOf<String>()
    if (!hasReceiveSMSPermissions(context))
        permissionsRequired += RECEIVE_SMS_PERMISSIONS_REQUIRED

    if (!hasReadExternalStoragePermissions(context))
        permissionsRequired += READ_EXTERNAL_STORAGE_REQUIRED

    return permissionsRequired
}

fun hasReceiveSMSPermissions(context: Context) = RECEIVE_SMS_PERMISSIONS_REQUIRED.all {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

fun hasReadExternalStoragePermissions(context: Context) = READ_EXTERNAL_STORAGE_REQUIRED.all {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}