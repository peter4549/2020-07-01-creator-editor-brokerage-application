package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.fragment_sign_up.*

class SignUpFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        edit_text_id.setRawInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)

        button_sign_up.setOnClickListener {
            createId()
        }
    }

    private fun createId() {
        val id = edit_text_id.text.toString()
        val password = edit_text_password.text.toString()

        when {
            id.isBlank() -> (activity as MainActivity).showToast("이메일을 입력해주세요.")
            password.isBlank() -> (activity as MainActivity).showToast("비밀번호를 입력해주세요.")
            else -> {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(id, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            (activity as MainActivity).showToast("아이디가 생성되었습니다.")
                            (activity as MainActivity).eventAfterLogin(task.result?.user)
                        } else
                            showExceptionMessage(task)
                    }
            }
        }
    }

    private fun showExceptionMessage(task: Task<AuthResult>) {
        try {
            throw task.exception!!
        } catch (e: FirebaseAuthWeakPasswordException) {
            (activity as MainActivity).showToast("너무 약한 비밀번호입니다.")
            edit_text_password.requestFocus()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            (activity as MainActivity).showToast("유효하지 않은 아이디입니다.")
            edit_text_id.requestFocus()
        } catch (e: FirebaseAuthUserCollisionException) {
            (activity as MainActivity).showToast("이미 존재하는 아이디입니다.")
            edit_text_id.requestFocus()
        } catch (e: Exception) {
            (activity as MainActivity).showToast("아이디 생성에 실패했습니다.")
        }
    }

    companion object {
        const val REQUEST_CODE_SIGN_IN = 1000
    }
}
