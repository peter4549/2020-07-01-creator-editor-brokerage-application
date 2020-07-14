package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.interfaces

import android.content.Context
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.KEY_USER_VERIFIED
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.PhoneAuthFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_USERS
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.firebase.firestore.FirebaseFirestore

class VerificationListener(private val context: Context): OnVerificationListener {
    private var count = 0
    private var codeFromFragment = ""
    private var codeFromReceiver = ""

    override fun onSetCodeFromFragment(code: String) {
        codeFromFragment = code
        ++count

        if (count >= 2) {
            onVerifyCode()
            count = 0
        }
    }

    override fun onSetCodeFromReceiver(code: String) {
        codeFromReceiver = code
        ++count

        if (count >= 2) {
            onVerifyCode()
            count = 0
        }
    }

    private fun onVerifyCode() {
        if (codeFromFragment == codeFromReceiver) {
            println("$TAG: Verification code matches")
            updateVerification()
        } else {
            showToast(context, "코드가 일치하지 않습니다.")
        }
    }

    private fun updateVerification() {
        val map = mutableMapOf<String, Any>()
        map[KEY_USER_VERIFIED] = true

        FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS)
            .document(MainActivity.currentUser?.uid.toString())
            .update(map)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast(context,"인증되었습니다.")
                } else {
                    showToast(context, "인증정보를 업데이트하는 중 문제가 발생했습니다. \n다시 진행해주십시오.")
                    println("${PhoneAuthFragment.TAG}: ${task.exception}")
                }
            }
    }

    companion object {
        const val TAG = "VerificationListener"
    }
}