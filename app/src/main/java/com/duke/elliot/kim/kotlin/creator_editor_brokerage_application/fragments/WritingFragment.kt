package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_writing.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class WritingFragment : Fragment() {

    private lateinit var selectedImageView: ImageView
    private val imageNames = mutableListOf<String?>(null, null, null)

    private val onImageViewClickListener = View.OnClickListener { view ->
        selectedImageView = view as ImageView
        openGallery()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_writing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image_view_add_1.setImageURI(null)
        image_view_add_2.setImageURI(null)
        image_view_add_3.setImageURI(null)

        image_view_add_1.setOnClickListener(onImageViewClickListener)
        image_view_add_2.setOnClickListener(onImageViewClickListener)
        image_view_add_3.setOnClickListener(onImageViewClickListener)

        button_upload.setOnClickListener {
            saveData()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            REQUEST_CODE_GALLERY -> {
                if (data != null)
                    setImage(data.data!!)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    private fun setImage(uri: Uri) {
        selectedImageView.setImageURI(null)
        selectedImageView.setImageURI(uri)
        selectedImageView.tag = uri

        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val fileName = "$timestamp.png"

        when (selectedImageView) {
            image_view_add_1 -> imageNames[0] = fileName
            image_view_add_2 -> imageNames[1] = fileName
            image_view_add_3 -> imageNames[2] = fileName
        }
    }

    private fun saveData() {
        if (edit_text_title.text.isBlank()) {
            (activity as MainActivity).showToast("제목을 입력해주세요.")
            return
        }

        var job = Job() as Job

        runBlocking {
            job = launch(Dispatchers.IO + job) {
                if (image_view_add_1.drawable != null && imageNames[0] != null)
                    uploadImage(image_view_add_1.tag as Uri, imageNames[0]!!)

                if (image_view_add_2.drawable != null && imageNames[1] != null)
                    uploadImage(image_view_add_2.tag as Uri, imageNames[1]!!)

                if (image_view_add_3.drawable != null && imageNames[2] != null)
                    uploadImage(image_view_add_3.tag as Uri, imageNames[2]!!)
            }

            job.join()

            val map = mutableMapOf<String, Any>()

            val title = edit_text_title.text.toString()
            val content = (edit_text_content.text ?: "").toString()

            map[TITLE] = title
            map[CONTENT] = content
            map[IMAGE_NAMES] = imageNames

            FirebaseFirestore.getInstance()
                .collection(PR_LIST)
                .document(FirebaseAuth.getInstance().uid.toString())
                .set(map)
                .addOnCompleteListener {  task ->
                    if (task.isSuccessful)
                        CoroutineScope(Dispatchers.Main).launch {
                            (activity as MainActivity).showToast("PR이 등록되었습니다.")
                        }
                    else {
                        CoroutineScope(Dispatchers.Main).launch {
                            (activity as MainActivity).showToast("등록에 실패했습니다.")
                        }
                        println("$TAG: ${task.exception}")
                    }
                }
        }
    }

    private fun uploadImage(uri: Uri, fileName: String) {
        val storageReference =
            FirebaseStorage.getInstance().reference
                .child(IMAGES)
                .child(MainActivity.currentUser!!.uid)
                .child(fileName)

        storageReference.putFile(uri).continueWithTask {
            return@continueWithTask storageReference.downloadUrl
        }.addOnSuccessListener {
            println("$TAG: Image uploaded, Uri: $it")
        }
    }

    companion object {
        const val TAG = "WritingFragment"

        const val REQUEST_CODE_GALLERY = 1000
    }
}
