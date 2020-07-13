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
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken
    private var resend = false
    lateinit var verificationListener: VerificationListener

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

        if (MainActivity.currentUser != null) {
            if (!(activity as MainActivity).isCurrentUserModelInitialized()) {
                (activity as MainActivity).showToast("먼저 프로필을 작성해주세요.")
                (activity as MainActivity).onBackPressed()
            }
        } else {
            (activity as MainActivity).showToast("먼저 로그인을 해주세요.")
            (activity as MainActivity).onBackPressed()
        }

        button_send_code.setOnClickListener {
            if (!resend) {
                setUpCallbacks()
                sendCode((activity as MainActivity).currentUserModel.phoneNumber)
            } else
                resendCode((activity as MainActivity).currentUserModel.phoneNumber)
        }

        button_test.setOnClickListener {
            (activity as MainActivity).onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()

        if (!isSmsReceiverRegistered) {
            requireContext().registerReceiver(smsReceiver, filter)
            isSmsReceiverRegistered = true
        }
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

    private fun sendCode(phoneNumber: String) {
        CoroutineScope(Dispatchers.IO).launch {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                PhoneNumberUtils.formatNumberToE164(phoneNumber, COUNTRY_CODE),
                timeout,
                TimeUnit.SECONDS,
                requireActivity(),
                callbacks
            // PhoneNumberUtils.formatNumberToE164(phoneNumber, COUNTRY_CODE)
            // 잘못된 양식의 번호로 보낼 시, 콜백에서 인증실패 로직은 동작함.,, 음..
            // 제대로 된놈 보냇더니 염병? 실행이 안된다..
            // 되던게 갑자기 안되니 ... 딱히 바꾼게 없는데. 추가 인증 이런거는 손 안댄거 같은데.
            // db 밀어보자.
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
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                verificationListener.onSetCodeFromFragment(p0.smsCode ?: "")
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                (activity as MainActivity).showToast("인증에 실패했습니다.")
                println("$TAG: ${p0.message}")
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                (activity as MainActivity).showToast("인증번호가 발송되었습니다.")
                verificationListener = VerificationListener(requireContext())
                resendingToken = p1
                resend = true
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
