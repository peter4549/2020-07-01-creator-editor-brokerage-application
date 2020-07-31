package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application

import android.content.Context
import com.google.firebase.storage.StorageException
import java.lang.Exception

class ErrorHandler {

    companion object {
        fun errorHandling(context: Context, tag: String, throwable: Throwable,
                          e: Exception?, message: String?) {
            if (message != null)
                showToast(context, message)

            when(e) {
                is StorageException -> fireStoreErrorHandling(context, e, tag, throwable)
                else -> printLog(throwable, e, tag)
            }
        }

        private fun fireStoreErrorHandling(context: Context, e: StorageException,
                                           tag: String, throwable: Throwable) {
            when(e.errorCode) {
                StorageException.ERROR_BUCKET_NOT_FOUND -> printLog(throwable, e, tag)
                StorageException.ERROR_CANCELED -> printLog(throwable, e, tag)
                StorageException.ERROR_INVALID_CHECKSUM -> printLog(throwable, e, tag)
                StorageException.ERROR_NOT_AUTHENTICATED -> printLog(throwable, e, tag)
                StorageException.ERROR_NOT_AUTHORIZED -> printLog(throwable, e, tag)
                StorageException.ERROR_OBJECT_NOT_FOUND -> printLog(throwable, e, tag)
                StorageException.ERROR_PROJECT_NOT_FOUND -> printLog(throwable, e, tag)
                StorageException.ERROR_QUOTA_EXCEEDED -> printLog(throwable, e, tag)
                StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> printLog(throwable, e, tag)
                else -> {
                    showToast(context, "알 수 없는 오류가 발생했습니다.")
                    printLog(throwable, e, tag)
                }
            }
        }

        private fun printLog(throwable: Throwable, e: Exception?, tag: String?) {
            println("error")
            println("tag: $tag")
            println("where: ${throwable.stackTrace[0]}")
            println("exception: $e")
            println("exception message: ${e?.message ?: "no exception message"}")
        }
    }
}

