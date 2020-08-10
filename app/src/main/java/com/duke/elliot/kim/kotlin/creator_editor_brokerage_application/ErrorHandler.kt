package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application

import android.content.Context
import com.google.firebase.storage.StorageException
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import java.io.IOException
import java.lang.Exception

class ErrorHandler {

    companion object {

        private lateinit var appName: String

        fun errorHandling(context: Context, e: Exception?, toastMessage: String?) {
            if (toastMessage != null)
                showToast(context, toastMessage)

            appName = context.getString(R.string.app_name)

            when(e) {
                is IOException -> ioExceptionHandling(context, e)
                is ResponseFailedException -> responseFailedExceptionHandling(context, e)
                is StorageException -> fireStoreErrorHandling(context, e)
                else -> printLog(e)
            }
        }

        private fun fireStoreErrorHandling(context: Context, e: StorageException) {
            when(e.errorCode) {
                StorageException.ERROR_BUCKET_NOT_FOUND -> printLog(e)
                StorageException.ERROR_CANCELED -> printLog(e)
                StorageException.ERROR_INVALID_CHECKSUM -> printLog(e)
                StorageException.ERROR_NOT_AUTHENTICATED -> printLog(e)
                StorageException.ERROR_NOT_AUTHORIZED -> printLog(e)
                StorageException.ERROR_OBJECT_NOT_FOUND -> printLog(e)
                StorageException.ERROR_PROJECT_NOT_FOUND -> printLog(e)
                StorageException.ERROR_QUOTA_EXCEEDED -> printLog(e)
                StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> printLog(e)
                else -> {
                    showToast(context, context.getString(R.string.unknown_error_occured))
                    printLog(e)
                }
            }
        }

        private fun ioExceptionHandling(context: Context, e: IOException) {
            when (e) {
                is java.net.UnknownHostException -> {
                    showToast(context, context.getString(R.string.request_check_internet_connection))
                    printLog(e)
                }
                else -> {
                    showToast(context, context.getString(R.string.unknown_error_occured))
                    printLog(e)
                }
            }
        }

        private fun responseFailedExceptionHandling(context: Context, e: ResponseFailedException) {
            val map: Map<*, *>? = Gson().fromJson(e.response.body?.string(), Map::class.java)
            val error = map?.get("error") as LinkedTreeMap<*, *>
            val code = error["code"] as Double?
            val message = error["message"] as String?
            val errors = (error["errors"] as ArrayList<*>)[0] as LinkedTreeMap<*, *>?
            // val domain = errors["domain"] as String
            val reason = errors?.get("reason") as String?

            println("MMMMMMMMMMMMM: $map")

            when(code) {
                400.0 -> {
                    when(reason) {
                        "badRequest" -> showToast(context, context.getString(R.string.invalid_api_key))
                        "invalidChannelId" -> showToast(context, context.getString(R.string.invalid_channel_id))
                    }

                    printLog(e, code, message, reason)
                }
                403.0 -> {
                    printLog(e, code, message, reason)
                    throw e
                }
                404.0 -> {
                    when(reason) {
                        "channelNotFound" -> showToast(context, context.getString(R.string.channel_id_not_found))
                        "playlistNotFound" -> showToast(context, context.getString(R.string.playlist_id_not_found))
                    }

                    printLog(e, code, message, reason)
                }
                else -> {
                    printLog(e)
                    throw e
                }
            }
        }

        private fun printLog(e: Exception?) {
            println("$appName exception")
            e?.printStackTrace()
        }

        private fun printLog(e: Exception?, code: Double?, message: String?, reason: String?) {
            println("$appName error")
            println("code: $code")
            println("message: $message")
            println("reason: $reason")
            e?.printStackTrace()
        }
    }
}

