package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_phone_auth.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import java.util.concurrent.TimeUnit


class PhoneAuthFragment : Fragment() {

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var smsCode: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        println("THISTHIS" + PhoneNumberUtils.formatNumberToE164("01043352595", COUNTRY_CODE))
        return inflater.inflate(R.layout.fragment_phone_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        button_send_code.setOnClickListener {
            sendCode()
        }

    }

    private fun sendCode() {
        setUpCallbacks()

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+821043352595",
            60,
            TimeUnit.SECONDS,
            requireActivity(),
            callbacks

            // PhoneNumberUtils.formatNumberToE164("01043352595", COUNTRY_CODE),
        )
    }

    private fun setUpCallbacks() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                (activity as MainActivity).showToast("인증번호가 발송되었습니다.")

                // 번호인증 절차. 여기서 수신한 코드와 아이디 등을 이용하여 인증절차 제작할 것.
                smsCode = p0.smsCode

            }

            override fun onVerificationFailed(p0: FirebaseException) {
                (activity as MainActivity).showToast("인증에 실패했습니다.")
                println("ERROR::" + p0.message)
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                // println("VERID " + p0)
            }
        }
    }

    companion object {
        // ISO 3166-1 two letters country code
        const val COUNTRY_CODE = "KR"
    }
}
