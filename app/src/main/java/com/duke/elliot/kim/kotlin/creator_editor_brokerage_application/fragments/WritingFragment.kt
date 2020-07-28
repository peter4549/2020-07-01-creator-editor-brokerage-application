package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.ObtainOAuthAccessTokenActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.*
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.hashString
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.PrModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.UserModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
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
    private lateinit var user: UserModel
    private val imageFileNames = mutableListOf<String?>(null, null, null)
    private val imageFileUris = mutableListOf<Uri?>(null, null, null)

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
            contentCategories[0] to ContentCategories.NONE,
            contentCategories[1] to ContentCategories.CAR,
            contentCategories[2] to ContentCategories.BEAUTY_FASHION,
            contentCategories[3] to ContentCategories.COMEDY,
            contentCategories[4] to ContentCategories.EDUCATION,
            contentCategories[5] to ContentCategories.ENTERTAINMENT,
            contentCategories[6] to ContentCategories.FAMILY_ENTERTAINMENT,
            contentCategories[7] to ContentCategories.MOVIE_ANIMATION,
            contentCategories[8] to ContentCategories.FOOD,
            contentCategories[9] to ContentCategories.GAME,
            contentCategories[10] to ContentCategories.KNOW_HOW_STYLE,
            contentCategories[11] to ContentCategories.MUSIC,
            contentCategories[12] to ContentCategories.NEWS_POLITICS,
            contentCategories[13] to ContentCategories.NON_PROFIT_SOCIAL_MOVEMENT,
            contentCategories[14] to ContentCategories.PEOPLE_BLOG,
            contentCategories[15] to ContentCategories.PETS_ANIMALS,
            contentCategories[16] to ContentCategories.SCIENCE_TECHNOLOGY,
            contentCategories[17] to ContentCategories.SPORTS,
            contentCategories[18] to ContentCategories.TRAVEL_EVENT
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

        Glide.with(image_view_add_1.context).clear(image_view_add_1)
        Glide.with(image_view_add_2.context).clear(image_view_add_2)
        Glide.with(image_view_add_3.context).clear(image_view_add_3)

        image_view_add_1.setOnClickListener(onImageViewClickListener)
        image_view_add_2.setOnClickListener(onImageViewClickListener)
        image_view_add_3.setOnClickListener(onImageViewClickListener)

        button_upload_youtube_videos.setOnClickListener {
            obtainOAuthAccessToken()
        }

        button_upload.setOnClickListener {
            saveData()
        }
    }

    override fun onResume() {
        super.onResume()
        user = MainActivity.currentUser!!
    }

    private fun setImageWithGlide(imageView: ImageView, uri: Uri?) {
        Glide.with(imageView.context)
            .load(uri)
            .error(R.drawable.ic_chat_64dp)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop())
            .into(imageView)
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

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,
            REQUEST_CODE_GALLERY
        )
    }

    private fun setImage(uri: Uri) {

        setImageWithGlide(selectedImageView, uri)

        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val fileName = "$timestamp.png"

        when (selectedImageView) {
            image_view_add_1 -> {
                imageFileNames[0] = fileName
                imageFileUris[0] = uri
            }
            image_view_add_2 -> {
                imageFileNames[1] = fileName
                imageFileUris[1] = uri
            }
            image_view_add_3 -> {
                imageFileNames[2] = fileName
                imageFileUris[2] = uri
            }
        }
    }

    private fun obtainOAuthAccessToken() {
        startActivity(Intent((activity as MainActivity), ObtainOAuthAccessTokenActivity::class.java))
    }

    private fun saveData() {
        if (edit_text_title.text.isBlank()) {
            showToast(requireContext(), "제목을 입력해주세요.")
            return
        }

        if (selectedCategory == "-") {
            showToast(requireContext(), "카테고리를 선택해주세요.")
            return
        }

        val job = Job() as Job

        CoroutineScope(Dispatchers.IO + job).launch {
            launch(Dispatchers.IO + job) {
                if (imageFileNames[0] != null && imageFileUris[0] != null)
                    uploadImage(imageFileUris[0]!!, imageFileNames[0]!!)

                if (imageFileNames[1] != null && imageFileUris[1] != null)
                    uploadImage(imageFileUris[1]!!, imageFileNames[1]!!)

                if (imageFileNames[2] != null && imageFileUris[2] != null)
                    uploadImage(imageFileUris[2]!!, imageFileNames[2]!!)
            }

            launch {
                val pr = PrModel()

                pr.publisherId = user.id
                pr.publisherName = user.publicName
                pr.occupation = "a"
                pr.category = categoryDocumentNames.getValue(selectedCategory)
                pr.title = edit_text_title.text.toString()
                pr.content = (edit_text_content.text ?: "").toString()
                pr.registrationTime =
                    SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
                pr.imageNames = imageFileNames

                val hashCode = hashString(user.id + pr.registrationTime).chunked(16)[0]
                FirebaseFirestore.getInstance()
                    .collection(COLLECTION_PR_LIST)
                    .document(hashCode)
                    .set(pr)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                            showToast(requireContext(), "PR이 등록되었습니다.")
                        else {
                            showToast(requireContext(), "등록에 실패했습니다.")
                            println("$TAG: ${task.exception}")
                        }
                    }
            }
        }
    }

    private fun uploadImage(uri: Uri, fileName: String) {
        val storageReference =
            FirebaseStorage.getInstance().reference
                .child(COLLECTION_IMAGES)
                .child(user.id)
                .child(fileName)

        storageReference.putFile(uri).continueWithTask {
            return@continueWithTask storageReference.downloadUrl
        }.addOnSuccessListener {
            println("$TAG: image uploaded, uri: $it")
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
    }
}