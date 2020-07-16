package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.*
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.UserModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_my_info.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MyInfoFragment : Fragment() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null
    private var profileImageFileName: String? = null
    private var profileImageFileUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_info, container, false)
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        simple_drawee_view_profile.setOnClickListener {
            openGallery()
        }

        button_verification.setOnClickListener {
            if (edit_text_phone_number.text.isNotBlank() && !verified) {
                val phoneAuthFragment = PhoneAuthFragment()
                phoneAuthFragment.setPhoneNumber(edit_text_phone_number.text.toString())
                (activity as MainActivity)
                    .startFragment(
                        phoneAuthFragment,
                        R.id.frame_layout_fragment_my_info,
                        MainActivity.PHONE_AUTH_FRAGMENT_TAG
                    )
            } else {
                if (edit_text_phone_number.text.isBlank())
                    showToast(requireContext(), "전화번호를 입력해주세요.")
                else if (verified)
                    showToast(requireContext(), "이미 인증하셨습니다.")
            }
        }

        button_save_data.setOnClickListener {
            if (verified)
                saveData()
            else
                showToast(requireContext(), "본인인증을 진행해주세요.")
        }

        button_logout.setOnClickListener {
            logout()
        }
    }

    override fun onResume() {
        super.onResume()
        if (firebaseAuth.currentUser != null) {
            if (MainActivity.currentUser != null) {

                if (MainActivity.currentUser!!.profileImageFileName.isNotBlank())
                    loadImage(MainActivity.currentUser!!.id, MainActivity.currentUser!!.profileImageFileName)
                edit_text_name.setText(MainActivity.currentUser!!.name)
                edit_text_public_name.setText(MainActivity.currentUser!!.publicName)
                edit_text_phone_number.setText(MainActivity.currentUser!!.phoneNumber)
                edit_text_pr.setText(MainActivity.currentUser!!.pr)


                setUI(MainActivity.currentUser!!)
                verified = MainActivity.currentUser!!.verified
            }
            else
                clearUI()
        } else
            clearUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_GALLERY -> {
                if (data != null) {
                    setImage(data.data!!)
                }
            }
        }
    }

    private fun setUI(userModel: UserModel) {

    }

    private fun clearUI() {
        profileImageFileName = null
        profileImageFileUri = null

        simple_drawee_view_profile.setImageURI("")
        edit_text_name.text.clear()
        edit_text_public_name.text.clear()
        edit_text_phone_number.text.clear()
        edit_text_pr.text.clear()
    }

    private fun loadImage(userId: String, profileImageFileName: String) {
        val storageReference = FirebaseStorage.getInstance().reference

        storageReference.child(COLLECTION_PROFILE_IMAGES)
            .child(userId).child(profileImageFileName).downloadUrl
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    simple_drawee_view_profile.setImageURI(task.result.toString())
                } else {
                    println("${PrListFragment.TAG}: ${task.exception}")
                }
            }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,
            REQUEST_CODE_GALLERY
        )
    }

    private fun setImage(uri: Uri) {
        profileImageFileUri = uri
        simple_drawee_view_profile.setImageURI(uri.toString())
    }

    private fun uploadImage(uri: Uri, fileName: String) = runBlocking {
        val storageReference =
            FirebaseStorage.getInstance().reference
                .child(COLLECTION_PROFILE_IMAGES)
                .child(firebaseAuth.currentUser!!.uid)
                .child(fileName)

        storageReference.putFile(uri).continueWithTask {
            return@continueWithTask storageReference.downloadUrl
        }.addOnSuccessListener {
            println("${WritingFragment.TAG}: Image uploaded, Uri: $it")
        }
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

        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {
            if (profileImageFileUri != null) {
                val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
                profileImageFileName = "$timestamp.png"
                uploadImage(profileImageFileUri!!, profileImageFileName!!)
            }

            val name = edit_text_name.text.toString()
            val phoneNumber = edit_text_phone_number.text.toString()
            val pr = (edit_text_pr.text ?: "").toString()
            val profileImageFileName = profileImageFileName
            val publicName = edit_text_public_name.text.toString()
            val user = UserModel()

            user.id = firebaseAuth.currentUser?.uid.toString()
            user.name = name
            user.phoneNumber = phoneNumber
            user.pr = pr
            user.profileImageFileName = profileImageFileName ?: ""
            user.publicName = publicName
            user.verified = verified

            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.pushToken = task.result?.token  // Set pushToken
                    println("$TAG: Token generated")

                    FirebaseFirestore.getInstance()
                        .collection(COLLECTION_USERS)
                        .document(firebaseAuth.currentUser?.uid.toString())
                        .set(user)
                        .addOnCompleteListener { setTask ->
                            if (setTask.isSuccessful) {
                                showToast(requireContext(), "프로필을 등록했습니다.")
                                MainActivity.currentUser = user
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
    }

    private fun logout() {
        clearUI()
        FirebaseAuth.getInstance().signOut()
        googleSignInClient?.signOut()
        LoginManager.getInstance().logOut()
    }

    companion object {
        const val TAG = "MyInfoFragment"

        var verified = false
    }
}
