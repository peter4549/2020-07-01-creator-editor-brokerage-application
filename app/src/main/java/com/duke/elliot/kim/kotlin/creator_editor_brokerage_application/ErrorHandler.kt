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

        fun errorHandling(context: Context, e: Exception?, throwable: Throwable, toastMessage: String?) {
            if (toastMessage != null)
                showToast(context, toastMessage)

            appName = context.getString(R.string.app_name)

            when(e) {
                is IOException -> ioExceptionHandling(context, e, throwable)
                is ResponseFailedException -> responseFailedExceptionHandling(context, e, throwable)
                is StorageException -> fireStoreErrorHandling(context, e, throwable)
                else -> printLog(e, throwable)
            }
        }

        private fun fireStoreErrorHandling(context: Context, e: StorageException, throwable: Throwable) {
            when(e.errorCode) {
                StorageException.ERROR_BUCKET_NOT_FOUND -> printLog(e, throwable)
                StorageException.ERROR_CANCELED -> printLog(e, throwable)
                StorageException.ERROR_INVALID_CHECKSUM -> printLog(e, throwable)
                StorageException.ERROR_NOT_AUTHENTICATED -> printLog(e, throwable)
                StorageException.ERROR_NOT_AUTHORIZED -> printLog(e, throwable)
                StorageException.ERROR_OBJECT_NOT_FOUND -> printLog(e, throwable)
                StorageException.ERROR_PROJECT_NOT_FOUND -> printLog(e, throwable)
                StorageException.ERROR_QUOTA_EXCEEDED -> printLog(e, throwable)
                StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> printLog(e, throwable)
                else -> {
                    showToast(context, context.getString(R.string.unknown_error_occured))
                    printLog(e, throwable)
                }
            }
        }

        private fun ioExceptionHandling(context: Context, e: IOException, throwable: Throwable) {
            when (e) {
                is java.net.UnknownHostException -> {
                    showToast(context, context.getString(R.string.request_check_internet_connection))
                    printLog(e, throwable)
                }
                else -> {
                    showToast(context, context.getString(R.string.unknown_error_occured))
                    printLog(e, throwable)
                }
            }
        }

        private fun responseFailedExceptionHandling(context: Context, e: ResponseFailedException, throwable: Throwable) {
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

                    printLog(e, throwable, code, message, reason)
                }
                403.0 -> {
                    printLog(e, throwable, code, message, reason)
                    throw e
                }
                404.0 -> {
                    when(reason) {
                        "channelNotFound" -> showToast(context, context.getString(R.string.channel_id_not_found))
                        "playlistNotFound" -> showToast(context, context.getString(R.string.playlist_id_not_found))
                    }

                    printLog(e, throwable, code, message, reason)
                }
                else -> {
                    printLog(e, throwable)
                    throw e
                }
            }
        }

        private fun printLog(e: Exception?, throwable: Throwable) {
            println("$appName error")
            println("Throwable stackTrace: ${throwable.stackTrace[0]}")
            e?.printStackTrace()
        }

        private fun printLog(e: Exception?, throwable: Throwable, code: Double?, message: String?, reason: String?) {
            println("$appName error")
            println("Throwable stackTrace: ${throwable.stackTrace[0]}")
            println("code: $code")
            println("message: $message")
            println("reason: $reason")
            e?.printStackTrace()
        }
    }
}

