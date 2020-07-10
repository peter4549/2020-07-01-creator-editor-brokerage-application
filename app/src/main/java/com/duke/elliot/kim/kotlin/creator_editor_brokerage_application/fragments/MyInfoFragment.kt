package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.UserModel
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_my_info.*

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

        button_verification.setOnClickListener {
            (activity as MainActivity)
                .startFragment(PhoneAuthFragment(), R.id.my_info_fragment_container_view)
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
            if ((activity as MainActivity).isCurrentUserModelInitialized())
                updateUI((activity as MainActivity).currentUserModel)
            else
                clearUI()
        } else {
            clearUI()
        }
    }

    private fun readData() {
        FirebaseFirestore.getInstance()
            .collection(USERS)
            .document(MainActivity.currentUser?.uid.toString())
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null)
                        updateUI(task.result!!)
                } else {
                    (activity as MainActivity).showToast("데이터를 읽어올 수 없습니다.")
                    println("$TAG: ${task.exception}")
                }
            }
    }

    private fun updateUI(documentSnapshot: DocumentSnapshot) {
        val map = documentSnapshot.data as Map<String, Any>

        MainActivity.publicName = map[PUBLIC_NAME].toString()

        edit_text_name.setText(map[NAME].toString())
        edit_text_public_name.setText(MainActivity.publicName)
        edit_text_phone_number.setText(map[PHONE_NUMBER].toString())
        edit_text_age.setText(map[AGE].toString())
        edit_text_pr.setText(map[PR].toString())

        radio_group_gender.check(map[GENDER].toString().toInt())
    }

    private fun updateUI(userModel: UserModel) {
        MainActivity.publicName = userModel.publicName

        edit_text_name.setText(userModel.name)
        edit_text_public_name.setText(userModel.publicName)
        edit_text_phone_number.setText(userModel.phoneNumber)
        edit_text_age.setText(userModel.age.toString())
        edit_text_pr.setText(userModel.pr)

        radio_group_gender.check(userModel.gender)
    }

    private fun clearUI() {
        edit_text_name.text.clear()
        edit_text_public_name.text.clear()
        edit_text_phone_number.text.clear()
        edit_text_age.text.clear()
        edit_text_pr.text.clear()
    }

    private fun saveData() {
        if (edit_text_name.text.isBlank()) {
            (activity as MainActivity).showToast("이름을 입력해주세요.")
            return
        }

        if (edit_text_public_name.text.isBlank()) {
            (activity as MainActivity).showToast("공개용 이름을 입력해주세요.")
            return
        }

        if (edit_text_phone_number.text.isBlank()) {
            (activity as MainActivity).showToast("전화번호를 입력해주세요.")
            return
        }

        val userModel = UserModel()
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
        userModel.publicName = publicName
        userModel.isVerified = false
        userModel.phoneNumber = phoneNumber
        userModel.age = age
        userModel.gender = gender
        userModel.pr = pr

        FirebaseFirestore.getInstance()
            .collection(USERS)
            .document(MainActivity.currentUser?.uid.toString())
            .set(userModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateUI(userModel)
                    (activity as MainActivity).currentUserModel = userModel
                } else {
                    (activity as MainActivity).showToast("데이터 저장에 실패했습니다.")
                    println("$TAG: ${task.exception}")
                }
            }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        googleSignInClient?.signOut()
        LoginManager.getInstance().logOut()
        (activity as MainActivity).eventAfterLogout()
    }

    companion object {
        const val TAG = "MyInfoFragment"

        const val MALE = 0
        const val FEMALE = 1
    }
}
