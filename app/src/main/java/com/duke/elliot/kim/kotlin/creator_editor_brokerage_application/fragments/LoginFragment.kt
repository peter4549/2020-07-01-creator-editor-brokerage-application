package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.REQUEST_CODE_SIGN_IN
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_sign_up_with_email.setOnClickListener {
            (activity as MainActivity)
                .startFragment(SignUpFragment(), R.id.login_fragment_container_view) }

        button_login_with_email.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch { loginWithEmail() }
        }

        button_login_with_google.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch { loginWithGoogle() }
        }

        button_login_with_facebook.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch { loginWithFacebook() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                println("$TAG: $e")
            }
        }
    }

    private fun loginWithEmail() {
        if (edit_text_id.text.isBlank()) {
            showToast(requireContext(), getString(R.string.request_id_message))
            return
        }

        if (edit_text_password.text.isBlank()) {
            showToast(requireContext(), getString(R.string.request_password_message))
            return
        }

        val email = edit_text_id.text.toString()
        val password = edit_text_password.text.toString()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    println("$TAG: login with email")
                else
                    showToast(requireContext(), getString(R.string.sign_in_failure_message))
            }
    }

    private fun loginWithGoogle() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireContext(),
            googleSignInOptions)
        val signInIntent = googleSignInClient?.signInIntent

        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                task ->
            if (task.isSuccessful)
                println("$TAG: login with Google")
            else {
                showToast(requireContext(), getString(R.string.authentication_failure_message))
                println("$TAG: ${task.exception}")
            }
        }
    }

    private fun loginWithFacebook() {
        LoginManager.getInstance().loginBehavior = LoginBehavior.WEB_VIEW_ONLY
        LoginManager.getInstance()
            .logInWithReadPermissions(requireActivity(), listOf("public_profile", "email"))
        LoginManager.getInstance().registerCallback((activity as MainActivity).callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                firebaseAuthWithFacebook(result)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {
                showToast(requireContext(), getString(R.string.sign_in_failure_message))
            }
        })
    }

    private fun firebaseAuthWithFacebook(result: LoginResult?) {
        val credential = FacebookAuthProvider.getCredential(result?.accessToken?.token!!)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                task ->
            if (task.isSuccessful)
                println("$TAG: login with Facebook")
            else {
                showToast(requireContext(), getString(R.string.authentication_failure_message))
                println("$TAG: ${task.exception}")
            }
        }
    }

    companion object {
        const val TAG = "LoginFragment"
    }
}
