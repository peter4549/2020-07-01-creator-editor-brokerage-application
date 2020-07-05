package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.broadcast_receiver.SmsReceiver
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_phone_auth.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class PhoneAuthFragment : Fragment(), SmsReceiver.OnVerifyCodeListener {

    private val filter = IntentFilter()
    private val smsReceiver = SmsReceiver()
    private val timeout = 60L
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var code: String? = null

    private var verificationId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        filter.addAction("android.provider.Telephony.SMS_RECEIVED")
        smsReceiver.setListener(this@PhoneAuthFragment)

        return inflater.inflate(R.layout.fragment_phone_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        button_send_code.setOnClickListener {
            setUpCallbacks()
            sendCode()
        }
    }

    override fun onVerifyCode(code: String) {
        if (this.code == code) {
            (activity as MainActivity).showToast("인증되었습니다.")
            // 유저의 인증정보 업데이트..
        }
    }

    private fun sendCode() {
        CoroutineScope(Dispatchers.IO).launch {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+821043352595",
                timeout,
                TimeUnit.SECONDS,
                requireActivity(),
                callbacks

                // PhoneNumberUtils.formatNumberToE164("01043352595", COUNTRY_CODE),
            )

            delay(timeout * 1000)

            if (isSmsReceiverRegistered) {
                requireContext().unregisterReceiver(smsReceiver)
                isSmsReceiverRegistered = false
            }
        }
    }

    private fun setUpCallbacks() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                code = p0.smsCode
                // smsCode = p0.smsCode


                //
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                (activity as MainActivity).showToast("인증에 실패했습니다.")
                println("$TAG: ${p0.message}")

                if (isSmsReceiverRegistered) {
                    requireContext().unregisterReceiver(smsReceiver)
                    isSmsReceiverRegistered = false
                }
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)

                if (!isSmsReceiverRegistered) {
                    requireContext().registerReceiver(smsReceiver, filter)
                    isSmsReceiverRegistered = true
                }

                (activity as MainActivity).showToast("인증번호가 발송되었습니다.")
                verificationId = p0
            }

        }
    }

    companion object {
        const val TAG = "PhoneAutoFragment"

        // ISO 3166-1 two letters country code
        const val COUNTRY_CODE = "KR"  // 국가별 string value로 처리할 것.

        var isSmsReceiverRegistered = false
    }
}
