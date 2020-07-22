package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.content.IntentFilter
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.broadcast_receiver.SmsReceiver
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.interfaces.VerificationListener
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_phone_auth.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class PhoneAuthFragment : Fragment(), SmsReceiver.OnSetCodeListener {

    private val filter = IntentFilter()
    private val smsReceiver = SmsReceiver()
    private val timeout = 60L
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var resend = false
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    lateinit var verificationListener: VerificationListener
    private var phoneNumber: String = ""

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
            if (!resend) {
                setUpCallbacks()
                sendCode(phoneNumber)
            } else
                resendCode(phoneNumber)
        }
    }

    override fun onStart() {
        super.onStart()

        if (!isSmsReceiverRegistered) {
            requireContext().registerReceiver(smsReceiver, filter)
            isSmsReceiverRegistered = true
        }
    }

    override fun onResume() {
        super.onResume()
        resend = false
        resendingToken = null
    }

    override fun onStop() {
        if (isSmsReceiverRegistered) {
            requireContext().unregisterReceiver(smsReceiver)
            isSmsReceiverRegistered = false
        }

        super.onStop()
    }

    override fun onSetCode(code: String) {
        verificationListener.onSetCodeFromReceiver(code)
    }

    fun setPhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber
    }

    private fun sendCode(phoneNumber: String) {
        CoroutineScope(Dispatchers.IO).launch {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                PhoneNumberUtils.formatNumberToE164(phoneNumber, COUNTRY_CODE),
                timeout,
                TimeUnit.SECONDS,
                requireActivity(),
                callbacks
            )
        }
    }

    private fun resendCode(phoneNumber: String) {
        CoroutineScope(Dispatchers.IO).launch {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                PhoneNumberUtils.formatNumberToE164(phoneNumber, COUNTRY_CODE),
                timeout,
                TimeUnit.SECONDS,
                requireActivity(),
                callbacks,
                resendingToken
            )
        }
    }

    private fun setUpCallbacks() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                if (phoneAuthCredential.smsCode != null)
                    verificationListener.onSetCodeFromFragment(phoneAuthCredential.smsCode!!)
                else
                    showToast(requireContext(), getString(R.string.code_lost))
            }

            override fun onVerificationFailed(e: FirebaseException) {
                when(e) {
                    is FirebaseAuthInvalidCredentialsException -> showToast(requireContext(), getString(R.string.invalid_request))
                    is FirebaseTooManyRequestsException -> showToast(requireContext(), getString(R.string.too_many_requests))
                    else -> showToast(requireContext(), getString(R.string.verification_failed))
                }

                println("$TAG: ${e.message}")
            }

            override fun onCodeSent(verificationId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken)
                showToast(requireContext(), getString(R.string.code_sent))
                verificationListener = VerificationListener(requireContext())
                resendingToken = forceResendingToken
                resend = true
            }
        }
    }

    companion object {
        const val TAG = "PhoneAutoFragment"

        // ISO 3166-1 two letters country code
        const val COUNTRY_CODE = "KR"  // 국가별 string value로 처리할 것. locale get default 확인해볼것.

        var isSmsReceiverRegistered = false
    }
}
