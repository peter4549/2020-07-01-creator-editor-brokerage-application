package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_PROFILE_IMAGES
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_USERS
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.REQUEST_CODE_GALLERY
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.UserModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
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
    private var profileImageFileDownloadUri: Uri? = null
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

        image_view_profile.setOnClickListener {
            openGallery()
        }

        // for test
        verified = true

        if (firebaseAuth.currentUser != null) {
            if (MainActivity.currentUser != null) {
                if (MainActivity.currentUser!!.profileImageFileDownloadUri.isNotBlank())
                    loadProfileImage(MainActivity.currentUser!!.profileImageFileDownloadUri)

                verified = MainActivity.currentUser!!.verified

                edit_text_name.setText(MainActivity.currentUser!!.name)
                edit_text_public_name.setText(MainActivity.currentUser!!.publicName)
                edit_text_phone_number.setText(MainActivity.currentUser!!.phoneNumber)
                edit_text_pr.setText(MainActivity.currentUser!!.pr)
            }
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
                uploadData()
            else
                showToast(requireContext(), "본인인증을 진행해주세요.")
        }

        button_logout.setOnClickListener {
            logout()
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_GALLERY -> {
                if (data != null)
                    setProfileImage(data.data!!)
            }
        }
    }

    private fun clearUI() {
        profileImageFileName = null
        profileImageFileUri = null

        Glide.with(image_view_profile.context)
            .clear(image_view_profile)
        edit_text_name.text.clear()
        edit_text_public_name.text.clear()
        edit_text_phone_number.text.clear()
        edit_text_pr.text.clear()
    }

    private fun loadProfileImage(profileImageFileUri: String) {
        Glide.with(image_view_profile.context)
            .load(profileImageFileUri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .error(R.drawable.ic_chat_64dp)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop())
            .into(image_view_profile)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,
            REQUEST_CODE_GALLERY
        )
    }

    private fun setProfileImage(uri: Uri) {
        Glide.with(image_view_profile.context).clear(image_view_profile)
        Glide.with(image_view_profile.context)
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .error(R.drawable.ic_chat_64dp)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop())
            .into(image_view_profile)
        profileImageFileUri = uri
    }

    private fun uploadData() = runBlocking {
        if (edit_text_name.text.isBlank()) {
            showToast(requireContext(), "이름을 입력해주세요.")
            return@runBlocking
        }

        if (edit_text_public_name.text.isBlank()) {
            showToast(requireContext(), "공개용 이름을 입력해주세요.")
            return@runBlocking
        }

        if (edit_text_phone_number.text.isBlank()) {
            showToast(requireContext(), "전화번호를 입력해주세요.")
            return@runBlocking
        }

        if (profileImageFileUri != null) {
            val timestamp =
                SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            profileImageFileName = "$timestamp.png"

            uploadDataWithProfileImage(profileImageFileUri!!, profileImageFileName!!)
        } else
            uploadDataWithoutProfileImage()
    }

    private fun uploadDataWithProfileImage(uri: Uri, fileName: String) {
        val storageReference =
            FirebaseStorage.getInstance().reference
                .child(COLLECTION_PROFILE_IMAGES)
                .child(firebaseAuth.currentUser!!.uid)
                .child(fileName)

        storageReference.putFile(uri).continueWithTask {
            return@continueWithTask storageReference.downloadUrl
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                profileImageFileDownloadUri = it.result
                println("$TAG: Image uploaded")
            } else {
                showToast(requireContext(), "이미지 업로드에 실패했습니다.")
                println("$TAG: ${it.exception}")
            }

            uploadDataWithoutProfileImage()
        }
    }

    private fun uploadDataWithoutProfileImage() {
        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {
            val user = UserModel()

            user.id = firebaseAuth.currentUser?.uid.toString()
            user.name = edit_text_name.text.toString()
            user.phoneNumber = edit_text_phone_number.text.toString()
            user.pr = (edit_text_pr.text ?: "").toString()
            user.profileImageFileDownloadUri = profileImageFileDownloadUri.toString()
            user.publicName = edit_text_public_name.text.toString()
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
