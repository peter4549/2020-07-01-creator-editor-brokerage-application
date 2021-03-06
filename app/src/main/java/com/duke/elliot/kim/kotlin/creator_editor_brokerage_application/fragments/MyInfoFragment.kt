package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_PARTNERS
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_PROFILE_IMAGES
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_USERS
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.REQUEST_CODE_GALLERY
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.PartnerModel
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
import kotlinx.android.synthetic.main.fragment_my_info_drawer.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MyInfoFragment : Fragment() {

    private lateinit var classes: ArrayList<String>
    private lateinit var selectedOccupation: String
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null
    private var profileImageFileDownloadUri: Uri? = null
    private var profileImageFileUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        classes = arrayListOf(
            "-",
            getString(R.string.creator),
            getString(R.string.editor)
        )

        val view = inflater.inflate(R.layout.fragment_my_info_drawer, container, false)

        (activity as MainActivity).setSupportActionBar(view.toolbar)
        (activity as MainActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)

        return view
    }

    override fun onResume() {
        super.onResume()
        if (MainActivity.currentUser != null) {
            if (MainActivity.currentUser!!.profileImageFileDownloadUri.isNotBlank())
                loadProfileImage(image_view_profile, MainActivity.currentUser!!.profileImageFileDownloadUri)

            verified = MainActivity.currentUser!!.verified

            edit_text_name.setText(MainActivity.currentUser!!.name)
            edit_text_public_name.setText(MainActivity.currentUser!!.publicName)
            edit_text_phone_number.setText(MainActivity.currentUser!!.phoneNumber)
            edit_text_pr.setText(MainActivity.currentUser!!.pr)
            button_partners_registration.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image_view_menu.setOnClickListener {
            drawer_layout_fragment_my_info.openDrawer(GravityCompat.END)
        }

        image_view_profile.setOnClickListener {
            openGallery()
        }

        text_view_sign_out.setOnClickListener {
            signOut()
        }

        spinner_occupation.adapter = SpinnerAdapter()
        spinner_occupation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedOccupation = classes[position]
            }
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

        button_partners_registration.setOnClickListener {
            confirmRegisterForPartners()
        }
    }

    override fun onStop() {
        super.onStop()
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

    private fun loadProfileImage(imageView: ImageView, profileImageFileUri: String) {
        Glide.with(imageView.context)
            .load(profileImageFileUri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.ic_add_to_photos_grey_80dp)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop())
            .into(imageView)
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
            .placeholder(R.drawable.ic_add_to_photos_grey_80dp)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop(), RoundedCorners(8))
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

        if (selectedOccupation == "-") {
            showToast(requireContext(), getString(R.string.request_class_selection))
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
                println("$TAG: image uploaded")
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

            user.channelIds = MainActivity.currentUser?.channelIds ?: mutableListOf()
            user.id = firebaseAuth.currentUser?.uid.toString()
            user.name = edit_text_name.text.toString()
            user.occupation = selectedOccupation
            user.phoneNumber = edit_text_phone_number.text.toString()
            user.pr = (edit_text_pr.text ?: "").toString()
            user.profileImageFileDownloadUri = profileImageFileDownloadUri.toString()
            user.publicName = edit_text_public_name.text.toString()
            user.registeredOnPartners = MainActivity.currentUser?.registeredOnPartners ?: false
            user.verified = verified

            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.pushToken = task.result?.token  // Set pushToken
                    println("$TAG: token generated")

                    val documentReference = FirebaseFirestore.getInstance()
                        .collection(COLLECTION_USERS)
                        .document(firebaseAuth.currentUser?.uid.toString())

                    if (MainActivity.currentUser == null) {
                        documentReference
                            .set(user)
                            .addOnCompleteListener { setTask ->
                                if (setTask.isSuccessful) {
                                    showToast(requireContext(), getString(R.string.profile_set))
                                    MainActivity.currentUser = user
                                    button_partners_registration.visibility = View.VISIBLE
                                } else {
                                    showToast(requireContext(), getString(R.string.profile_set_failed))
                                    println("$TAG: ${setTask.exception}")
                                }
                            }
                    } else {
                        documentReference
                            .update(user.toHashMap())
                            .addOnCompleteListener { setTask ->
                                if (setTask.isSuccessful) {
                                    showToast(requireContext(), getString(R.string.profile_updated))
                                    MainActivity.currentUser = user
                                } else {
                                    showToast(requireContext(), getString(R.string.profile_update_failed))
                                    println("$TAG: ${setTask.exception}")
                                }
                            }
                    }
                } else {
                    showToast(requireContext(), getString(R.string.token_generation_failed))
                    println("$TAG: token generation failed")
                }
            }
        }
    }

    private fun confirmRegisterForPartners() {
        val builder = AlertDialog.Builder(requireContext())
        val message =
            if (MainActivity.currentUser!!.registeredOnPartners)
                getString(R.string.ask_for_update_on_partners)
            else
                getString(R.string.ask_for_register_on_partners)

        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.login)) { _, _ ->
            registerForPartners()
        }.setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .create().show()
    }

    private fun registerForPartners() {
        if (MainActivity.currentUser != null) {
            val documentReference = FirebaseFirestore.getInstance().collection(COLLECTION_PARTNERS)
                .document(MainActivity.currentUser!!.id)
            val partner = PartnerModel()
            partner.occupation = MainActivity.currentUser!!.occupation
            partner.profileImageUri = MainActivity.currentUser!!.profileImageFileDownloadUri
            partner.publicName = MainActivity.currentUser!!.publicName
            partner.stars = MainActivity.currentUser!!.stars
            partner.statusMessage = "Status Message"
            partner.uid = MainActivity.currentUser!!.id

            if (MainActivity.currentUser!!.registeredOnPartners) {
                documentReference.update(partner.toHashMap())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showToast(requireContext(), getString(R.string.updated_on_partners))
                            updateRegisterOnPartners(true)
                        } else {
                            showToast(requireContext(), getString(R.string.failed_to_update_on_partners))
                            println("$TAG: ${task.exception}")
                        }
                    }
            } else {
                documentReference.set(partner)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showToast(requireContext(), getString(R.string.register_on_partners))
                            updateRegisterOnPartners(true)
                        } else {
                            showToast(requireContext(), getString(R.string.failed_to_register_on_partners))
                            println("$TAG: ${task.exception}")
                        }
                    }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateRegisterOnPartners(registered: Boolean) {
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS).document(MainActivity.currentUser!!.id)
            .update(hashMapOf(UserModel.KEY_REGISTERED_ON_PARTNERS to registered) as HashMap<String, Any>)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    MainActivity.currentUser!!.registeredOnPartners = true
                    println("$TAG: \"registerOnPartners\" updated")
                }
                else
                    println("$TAG: ${task.exception}")
            }
    }

    private fun signOut() {
        clearUI()
        FirebaseAuth.getInstance().signOut()
        googleSignInClient?.signOut()
        LoginManager.getInstance().logOut()
    }

    inner class SpinnerAdapter: BaseAdapter() {

        private val inflater =
            (activity as MainActivity).getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val holder: ViewHolder

            if (convertView == null) {
                val view = inflater.inflate(R.layout.item_view_spinner, parent, false)
                holder = ViewHolder()
                holder.textView = view.findViewById(R.id.text_view_spinner)
                holder.textView.tag = holder
            } else
                holder = convertView.tag as ViewHolder

            holder.textView.text = classes[position]

            return holder.textView
        }

        override fun getItem(position: Int): Any {
            return classes[position]
        }

        // Unused
        override fun getItemId(position: Int): Long {
            return 0L
        }

        override fun getCount(): Int {
            return classes.count()
        }

        inner class ViewHolder {
            lateinit var textView: TextView
        }
    }

    companion object {
        const val TAG = "MyInfoFragment"

        var verified = false
    }
}
