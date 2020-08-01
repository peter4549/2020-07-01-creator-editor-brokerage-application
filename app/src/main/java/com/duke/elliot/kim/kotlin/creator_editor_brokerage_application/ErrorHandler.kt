package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application

import android.content.Context
import com.google.firebase.storage.StorageException
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import okhttp3.ResponseBody
import java.io.IOException
import java.lang.Exception

class ErrorHandler {

    companion object {

        private lateinit var appName: String

        fun errorHandling(context: Context, tag: String, throwable: Throwable,
                          e: Exception?, message: String?) {
            if (message != null)
                showToast(context, message)

            appName = context.getString(R.string.app_name)

            when(e) {
                is IOException -> ioExceptionHandling(context, e, tag, throwable)
                is ResponseFailedException -> responseFailedExceptionHandling(context, e, tag, throwable)
                is StorageException -> fireStoreErrorHandling(context, e, tag, throwable)
                else -> printLog(tag, throwable, e)
            }
        }

        private fun fireStoreErrorHandling(context: Context, e: StorageException,
                                           tag: String, throwable: Throwable) {
            when(e.errorCode) {
                StorageException.ERROR_BUCKET_NOT_FOUND -> printLog(tag, throwable, e)
                StorageException.ERROR_CANCELED -> printLog(tag, throwable, e)
                StorageException.ERROR_INVALID_CHECKSUM -> printLog(tag, throwable, e)
                StorageException.ERROR_NOT_AUTHENTICATED -> printLog(tag, throwable, e)
                StorageException.ERROR_NOT_AUTHORIZED -> printLog(tag, throwable, e)
                StorageException.ERROR_OBJECT_NOT_FOUND -> printLog(tag, throwable, e)
                StorageException.ERROR_PROJECT_NOT_FOUND -> printLog(tag, throwable, e)
                StorageException.ERROR_QUOTA_EXCEEDED -> printLog(tag, throwable, e)
                StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> printLog(tag, throwable, e)
                else -> {
                    showToast(context, context.getString(R.string.unknown_error_occured))
                    printLog(tag, throwable, e)
                }
            }
        }

        private fun ioExceptionHandling(context: Context, e: IOException,
                                        tag: String, throwable: Throwable) {
            when (e) {
                is java.net.UnknownHostException -> {
                    showToast(context, context.getString(R.string.request_check_internet_connection))
                    printLog(tag, throwable, e)
                }
                else -> {
                    showToast(context, context.getString(R.string.unknown_error_occured))
                    printLog(tag, throwable, e)
                }
            }
        }

        private fun responseFailedExceptionHandling(context: Context, e: ResponseFailedException,
                                                    tag: String, throwable: Throwable) {
            val map: Map<*, *>? = Gson().fromJson(e.responseBody, Map::class.java)
            val error = map?.get("error") as LinkedTreeMap<*, *>
            val code = error["code"] as Double
            val message = error["message"] as String
            val errors = (error["errors"] as ArrayList<*>)[0] as LinkedTreeMap<*, *>
            // val domain = errors["domain"] as String
            val reason = errors["reason"] as String

            when(code) {
                400.0 -> {
                    printLog(tag, throwable, code, message, reason)
                    showToast(context, context.getString(R.string.invalid_api_key))
                }
                403.0 -> {
                    printLog(tag, throwable, code, message, reason)
                    throw e
                }
                else -> {
                    printLog(tag, throwable, e)
                    throw e
                }
            }


        }

        private fun printLog(tag: String, throwable: Throwable, e: Exception?) {
            println("$appName error")
            println("tag: $tag")
            println("where: ${throwable.stackTrace[0]}")
            println("exception: $e")
            println("exception message: ${e?.message ?: "no exception message"}")
        }

        private fun printLog(tag: String, throwable: Throwable, code: Double, message: String, reason: String) {
            println("$appName error")
            println("tag: $tag")
            println("where: ${throwable.stackTrace[0]}")
            println("exception message: $message")
            println("code: $code")
            println("reason: $reason")
        }
    }
}

