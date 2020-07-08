package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

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

    private lateinit var categoryDocumentNames: Map<String, String>
    private lateinit var contentCategories: ArrayList<String>
    private lateinit var selectedCategory: String
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
        contentCategories = arrayListOf(
            "-",
            getString(R.string.category_car),
            getString(R.string.category_beauty_fashion),
            getString(R.string.category_comedy),
            getString(R.string.category_education),
            getString(R.string.category_entertainment),
            getString(R.string.category_family_entertainment),
            getString(R.string.category_movie_animation),
            getString(R.string.category_food),
            getString(R.string.category_game),
            getString(R.string.category_know_how_style),
            getString(R.string.category_music),
            getString(R.string.category_news_politics),
            getString(R.string.category_non_profit_social_movement),
            getString(R.string.category_people_blog),
            getString(R.string.category_pets_animals),
            getString(R.string.category_science_technology),
            getString(R.string.category_sports),
            getString(R.string.category_travel_event)
        )

        categoryDocumentNames = mapOf(
            contentCategories[0] to "category_car",
            contentCategories[1] to "category_beauty_fashion",
            contentCategories[2] to "category_comedy",
            contentCategories[3] to "category_education",
            contentCategories[4] to "category_entertainment",
            contentCategories[5] to "category_family_entertainment",
            contentCategories[6] to "category_movie_animation",
            contentCategories[7] to "category_food",
            contentCategories[8] to "category_game",
            contentCategories[9] to "category_know_how_style",
            contentCategories[10] to "category_music",
            contentCategories[11] to "category_news_politics",
            contentCategories[12] to "category_non_profit_social_movement",
            contentCategories[13] to "category_people_blog",
            contentCategories[14] to "category_pets_animals",
            contentCategories[15] to "category_science_technology",
            contentCategories[16] to "category_sports",
            contentCategories[17] to "category_travel_event"
        )

        selectedCategory = contentCategories[0]

        return inflater.inflate(R.layout.fragment_writing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinner_categories.adapter = SpinnerAdapter()
        spinner_categories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = contentCategories[position]
            }
        }

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

        if (selectedCategory == "-") {
            (activity as MainActivity).showToast("카테고리를 선택해주세요.")
            return
        }

        val registrationTime =
            SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
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
            val userId = MainActivity.currentUser?.uid!!
            val categoryDocumentName = categoryDocumentNames.getValue(selectedCategory)
            val title = edit_text_title.text.toString()
            val content = (edit_text_content.text ?: "").toString()

            map[USER_ID] = userId
            map[CATEGORY] = categoryDocumentName
            map[TITLE] = title
            map[CONTENT] = content
            map[IMAGE_NAMES] = imageNames
            map[REGISTRATION_TIME] = registrationTime

            FirebaseFirestore.getInstance()
                .collection(PR_LIST)
                .document(categoryDocumentName)
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

            holder.textView.text = contentCategories[position]

            return holder.textView
        }

        override fun getItem(position: Int): Any {
            return contentCategories[position]
        }

        // Unused
        override fun getItemId(position: Int): Long {
            return 0L
        }

        override fun getCount(): Int {
            return contentCategories.count()
        }

        inner class ViewHolder {
            lateinit var textView: TextView
        }
    }

    companion object {
        const val TAG = "WritingFragment"

        const val REQUEST_CODE_GALLERY = 1000
    }
}
