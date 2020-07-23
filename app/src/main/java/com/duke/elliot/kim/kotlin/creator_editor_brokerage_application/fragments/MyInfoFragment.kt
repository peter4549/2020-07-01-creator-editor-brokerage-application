package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
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
import kotlinx.android.synthetic.main.fragment_my_info.view.*
import kotlinx.android.synthetic.main.fragment_my_info_navigation_drawer.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MyInfoFragment : Fragment() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null
    private var profileImageFileDownloadUri: Uri? = null
    private var profileImageFileUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_info_navigation_drawer, container, false)

        if (firebaseAuth.currentUser != null) {
            if (MainActivity.currentUser != null) {
                if (MainActivity.currentUser!!.profileImageFileDownloadUri.isNotBlank())
                    loadProfileImage(view, MainActivity.currentUser!!.profileImageFileDownloadUri)

                verified = MainActivity.currentUser!!.verified

                view.edit_text_name.setText(MainActivity.currentUser!!.name)
                view.edit_text_public_name.setText(MainActivity.currentUser!!.publicName)
                view.edit_text_phone_number.setText(MainActivity.currentUser!!.phoneNumber)
                view.edit_text_pr.setText(MainActivity.currentUser!!.pr)
            }
        }

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        image_view_menu.setOnClickListener {
            drawer_layout_fragment_my_info.openDrawer(GravityCompat.END)
        }

        image_view_profile.setOnClickListener {
            openGallery()
        }

        // for test
        verified = true

        button_verification.setOnClickListener {
            if (edit_text_phone_number.text.isNotBlank() && !verified) {
                val phoneAuthFragment = PhoneAuthFragment()
                phoneAuthFragment.setPhoneNumber(edit_text_phone_number.text.toString())
                (activity as MainActivity)
                    .startFragment(
                        phoneAuthFragment,
                        R.id.drawer_layout_fragment_my_info,
                        MainActivity.PHONE_AUTH_FRAGMENT_TAG
                    )
            } else {
                if (edit_text_phone_number.text.isBlank())
                    showToast(requireContext(), getString(R.string.request_phone_number))
                else if (verified)
                    showToast(requireContext(), getString(R.string.already_verified))
            }
        }

        button_save_data.setOnClickListener {
            if (verified)
                uploadData()
            else
                showToast(requireContext(), getString(R.string.request_verification))
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
        profileImageFileUri = null

        Glide.with(image_view_profile.context)
            .clear(image_view_profile)
        edit_text_name.text.clear()
        edit_text_public_name.text.clear()
        edit_text_phone_number.text.clear()
        edit_text_pr.text.clear()
    }

    private fun loadProfileImage(view: View, profileImageFileUri: String) {
        Glide.with(view.image_view_profile.context)
            .load(profileImageFileUri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .error(R.drawable.ic_chat_64dp)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop())
            .into(view.image_view_profile)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,
            REQUEST_CODE_GALLERY
        )
    }

    private fun setProfileImage(uri: Uri) {
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
            showToast(requireContext(), getString(R.string.request_name))
            return@runBlocking
        }

        if (edit_text_public_name.text.isBlank()) {
            showToast(requireContext(), getString(R.string.request_public_name))
            return@runBlocking
        }

        if (edit_text_phone_number.text.isBlank()) {
            showToast(requireContext(), getString(R.string.request_phone_number))
            return@runBlocking
        }

        if (profileImageFileUri != null) {


            uploadDataWithProfileImage(profileImageFileUri!!)
        } else
            uploadDataWithoutProfileImage()
    }

    private fun uploadDataWithProfileImage(uri: Uri) {
        val timestamp =
            SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val profileImageFileName = "$timestamp.png"
        val storageReference =
            FirebaseStorage.getInstance().reference
                .child(COLLECTION_PROFILE_IMAGES)
                .child(firebaseAuth.currentUser!!.uid)
                .child(profileImageFileName)

        storageReference.putFile(uri).continueWithTask {
            return@continueWithTask storageReference.downloadUrl
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                profileImageFileDownloadUri = it.result
                println("$TAG: Image uploaded")
            } else {
                showToast(requireContext(), getString(R.string.image_upload_failed))
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
                    println("$TAG: token generated")

                    FirebaseFirestore.getInstance()
                        .collection(COLLECTION_USERS)
                        .document(firebaseAuth.currentUser?.uid.toString())
                        .set(user)
                        .addOnCompleteListener { setTask ->
                            if (setTask.isSuccessful) {
                                showToast(requireContext(), getString(R.string.profile_uploaded))
                                MainActivity.currentUser = user
                            } else {
                                showToast(requireContext(), getString(R.string.profile_upload_failed))
                                println("$TAG: ${setTask.exception}")
                            }
                        }
                } else {
                    showToast(requireContext(), getString(R.string.token_generation_failure_message))
                    println("$TAG: token generation failed")
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
