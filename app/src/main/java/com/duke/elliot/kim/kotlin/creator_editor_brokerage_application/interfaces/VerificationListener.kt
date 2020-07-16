package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.interfaces

import android.content.Context
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.PhoneAuthFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_USERS
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.MyInfoFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.UserModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.firebase.auth.FirebaseAuth
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
            showToast(context, "인증되었습니다.")
            println("$TAG: Verification code matches")
            MyInfoFragment.verified = true
        } else {
            showToast(context, "코드가 일치하지 않습니다.")
        }
    }

    companion object {
        const val TAG = "VerificationListener"
    }
}