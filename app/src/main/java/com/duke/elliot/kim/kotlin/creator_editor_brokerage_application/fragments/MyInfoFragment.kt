package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.*
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.UserDataModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_my_info.*
import kotlinx.coroutines.*

class MyInfoFragment : Fragment() {

    private var googleSignInClient: GoogleSignInClient? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)

        return inflater.inflate(R.layout.fragment_my_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        simple_drawee_view_profile.setOnClickListener {
            openGallery()
        }

        button_verification.setOnClickListener {
            (activity as MainActivity)
                .startFragment(PhoneAuthFragment(),
                    R.id.frame_layout_fragment_my_info,
                    MainActivity.PHONE_AUTH_FRAGMENT_TAG)
        }

        button_save_data.setOnClickListener {
            saveData()
        }

        button_logout.setOnClickListener {
            logout()
        }
    }

    override fun onResume() {
        super.onResume()

        if (MainActivity.currentUser != null) {
            if (MainActivity.currentUserDataModel != null)
                updateUI(MainActivity.currentUserDataModel!!)
            else
                clearUI()
        } else
            clearUI()

        if (MainActivity.currentUserDataModel == null ||
            MainActivity.currentUserDataModel?.isVerified == true) {
            button_verification.isEnabled = false
            button_verification.setTextColor(ContextCompat.getColor(requireContext(),
                R.color.colorDisabledButtonText))
        } else {
            button_verification.isEnabled = true
            button_verification.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorAccent
                )
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_GALLERY -> {
                if (data != null)
                    setImage(data.data!!)
            }
        }
    }

    private fun readData() {
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS)
            .document(MainActivity.currentUser?.uid.toString())
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null)
                        updateUI(task.result!!)
                } else {
                    showToast(requireContext(), "데이터를 읽어올 수 없습니다.")
                    println("$TAG: ${task.exception}")
                }
            }
    }

    private fun updateUI(documentSnapshot: DocumentSnapshot) {
        val map = documentSnapshot.data as Map<String, Any>
        edit_text_name.setText(map[KEY_USER_NAME].toString())
        edit_text_public_name.setText(map[KEY_USER_PUBLIC_NAME].toString())
        edit_text_phone_number.setText(map[KEY_USER_PHONE_NUMBER].toString())
        edit_text_age.setText(map[KEY_USER_AGE].toString())
        edit_text_pr.setText(map[KEY_USER_PR].toString())

        radio_group_gender.check(map[KEY_USER_GENDER].toString().toInt())
    }

    private fun updateUI(userDataModel: UserDataModel) {
        edit_text_name.setText(userDataModel.name)
        edit_text_public_name.setText(userDataModel.publicName)
        edit_text_phone_number.setText(userDataModel.phoneNumber)
        edit_text_age.setText(userDataModel.age.toString())
        edit_text_pr.setText(userDataModel.pr)
        radio_group_gender.check(userDataModel.gender)
    }

    private fun clearUI() {
        edit_text_name.text.clear()
        edit_text_public_name.text.clear()
        edit_text_phone_number.text.clear()
        edit_text_age.text.clear()
        edit_text_pr.text.clear()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,
            REQUEST_CODE_GALLERY
        )
    }

    private fun setImage(uri: Uri) {
        simple_drawee_view_profile.setImageURI(uri.toString())
    }

    private fun saveData() {
        if (edit_text_name.text.isBlank()) {
            showToast(requireContext(), "이름을 입력해주세요.")
            return
        }

        if (edit_text_public_name.text.isBlank()) {
            showToast(requireContext(), "공개용 이름을 입력해주세요.")
            return
        }

        if (edit_text_phone_number.text.isBlank()) {
            showToast(requireContext(), "전화번호를 입력해주세요.")
            return
        }

        val userModel = UserDataModel()
        val name = edit_text_name.text.toString()
        val publicName = edit_text_public_name.text.toString()
        val phoneNumber = edit_text_phone_number.text.toString()
        val age = (edit_text_age.text ?: "0").toString().toInt()
        val gender = when(radio_group_gender.checkedRadioButtonId) {
            R.id.radio_button_male -> MALE
            else -> FEMALE
        }
        val pr = (edit_text_pr.text ?: "").toString()

        userModel.name = name
        userModel.userId = MainActivity.currentUser?.uid.toString()
        userModel.publicName = publicName
        userModel.isVerified = false
        userModel.phoneNumber = phoneNumber
        userModel.age = age
        userModel.gender = gender
        userModel.pr = pr

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userModel.pushToken = task.result?.token  // Set pushToken
                println("$TAG: Token generated")

                FirebaseFirestore.getInstance()
                    .collection(COLLECTION_USERS)
                    .document(MainActivity.currentUser?.uid.toString())
                    .set(userModel)
                    .addOnCompleteListener { setTask ->
                        if (setTask.isSuccessful) {
                            updateUI(userModel)
                            MainActivity.currentUserDataModel = userModel
                        } else {
                            showToast(requireContext(), "데이터 저장에 실패했습니다.")
                            println("$TAG: ${setTask.exception}")
                        }
                    }
            } else {
                showToast(requireContext(), "토큰 생성에 실패했습니다.")
                println("$TAG: Token generation failed")
            }
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        googleSignInClient?.signOut()
        LoginManager.getInstance().logOut()
    }

    companion object {
        const val TAG = "MyInfoFragment"

        const val MALE = 0
        const val FEMALE = 1
    }
}
