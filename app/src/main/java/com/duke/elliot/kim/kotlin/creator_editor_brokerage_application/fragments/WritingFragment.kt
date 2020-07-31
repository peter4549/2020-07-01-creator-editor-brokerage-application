package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.YouTubeChannelsActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.*
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.dialog_fragments.SelectVideoSourceDialogFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.hashString
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.PrModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_writing.*
import kotlinx.android.synthetic.main.fragment_writing.image_view_add_1
import kotlinx.android.synthetic.main.fragment_writing.image_view_add_2
import kotlinx.android.synthetic.main.fragment_writing.view.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class WritingFragment : Fragment() {

    private lateinit var categoryDocumentNames: Map<String, String>
    private lateinit var contentCategories: ArrayList<String>
    private lateinit var selectedCategory1: String
    private lateinit var selectedCategory2: String
    private lateinit var selectedImageView: ImageView
    private val imageFileNames = mutableListOf<String?>(null, null, null)
    private val imageFileUris = mutableListOf<Uri?>(null, null, null)
    private val youtubeVideoIds = mutableListOf<String?>(null, null, null)
    private var localVideosCount = 0
    private var savedLocalVideosCount = 0
    private var thumbnailUri = ""

    private val onImageViewClickListener = View.OnClickListener { view ->
        selectedImageView = view as ImageView
        val selectVideoSourceDialogFragment = SelectVideoSourceDialogFragment()

        selectVideoSourceDialogFragment.show(requireActivity().supportFragmentManager, TAG)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_writing, container, false)
        (requireActivity() as MainActivity).setSupportActionBar(view.toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        view.toolbar.title = "PR 작성"

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

        selectedCategory1 = contentCategories[0]
        selectedCategory2 = contentCategories[0]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinner_categories_1.adapter = SpinnerAdapter()
        spinner_categories_1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory1 = contentCategories[position]
            }
        }

        spinner_categories_2.adapter = SpinnerAdapter()
        spinner_categories_2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory2 = contentCategories[position]
            }
        }

        Glide.with(image_view_add_1.context).clear(image_view_add_1)
        Glide.with(image_view_add_2.context).clear(image_view_add_2)
        Glide.with(image_view_add_3.context).clear(image_view_add_3)

        image_view_add_1.setOnClickListener(onImageViewClickListener)
        image_view_add_2.setOnClickListener(onImageViewClickListener)
        image_view_add_3.setOnClickListener(onImageViewClickListener)

        button_upload.setOnClickListener {
            button_upload.isEnabled = false
            saveData()
        }
    }

    private fun setImageWithGlide(imageView: ImageView, uri: Uri?) {
        Glide.with(imageView.context)
            .load(uri)
            .error(R.drawable.ic_sentiment_dissatisfied_grey_24dp)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop())
            .listener(object: RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    showToast(requireContext(), "이미지 로드에 실패했습니다.")
                    println("$TAG: ${e?.message}")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    imageViewCrossFadeIn(imageView)
                    return false
                }
            })
            .into(imageView)
    }

    private fun imageViewCrossFadeIn(imageView: ImageView) {
        val shortAnimationTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        when (imageView) {
            image_view_add_1 -> {
                image_view_add_2.apply {
                    alpha = 0F
                    visibility = View.VISIBLE

                    animate().alpha(1F)
                        .setDuration(shortAnimationTime.toLong())
                        .setListener(null)
                }
            }

            image_view_add_2 -> {
                image_view_add_3.apply {
                    alpha = 0F
                    visibility = View.VISIBLE

                    animate().alpha(1F)
                        .setDuration(shortAnimationTime.toLong())
                        .setListener(null)
                }
            }

            else -> {
                println("$TAG: invalid ImageView")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_GALLERY -> {
                if (data != null)
                    setImage(data.data!!, ImageSource.DEVICE)
            }
            REQUEST_CODE_YOUTUBE_CHANNELS -> {
                if (data != null) {
                    val thumbnailUri = data.getStringExtra(YouTubeVideosFragment.KEY_THUMBNAIL_URI)
                    val videoId = data.getStringExtra(YouTubeVideosFragment.KEY_VIDEO_ID)
                    setImage(Uri.parse(thumbnailUri), ImageSource.YOUTUBE, videoId)
                }
            }
        }
    }

    fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,
            REQUEST_CODE_GALLERY
        )
    }

    private fun setImage(uri: Uri, imageSource: ImageSource, youtubeVideoId: String? = null) {
        setImageWithGlide(selectedImageView, uri)

        if (imageSource == ImageSource.DEVICE) {
            val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val fileName = "$timestamp.png"

            when (selectedImageView) {
                image_view_add_1 -> {
                    youtubeVideoIds[0] = null
                    imageFileNames[0] = fileName
                    imageFileUris[0] = uri
                }
                image_view_add_2 -> {
                    youtubeVideoIds[1] = null
                    imageFileNames[1] = fileName
                    imageFileUris[1] = uri
                }
                image_view_add_3 -> {
                    youtubeVideoIds[2] = null
                    imageFileNames[2] = fileName
                    imageFileUris[2] = uri
                }
            }
        } else if (imageSource == ImageSource.YOUTUBE) {
            when (selectedImageView) {
                image_view_add_1 -> {
                    imageFileNames[0] = null
                    imageFileUris[0] = null
                    thumbnailUri = uri.toString()
                    youtubeVideoIds[0] = youtubeVideoId
                }
                image_view_add_2 -> {
                    imageFileNames[1] = null
                    imageFileUris[1] = null
                    youtubeVideoIds[1] = youtubeVideoId
                }
                image_view_add_3 -> {
                    imageFileNames[2] = null
                    imageFileUris[2] = null
                    youtubeVideoIds[2] = youtubeVideoId
                }
            }
        }
    }

    fun openYouTubeChannels() {
        val intent = Intent((requireActivity() as MainActivity), YouTubeChannelsActivity::class.java)

        intent.action = ACTION_FROM_WRITING_FRAGMENT
        intent.putExtra(KEY_CHANNELS, MainActivity.currentUser!!.channelIds.toTypedArray())
        startActivityForResult(intent, REQUEST_CODE_YOUTUBE_CHANNELS)
    }

    private fun saveData() {
        if (edit_text_title.text.isBlank()) {
            showToast(requireContext(), "제목을 입력해주세요.")
            return
        }

        if (selectedCategory1 == "-" && selectedCategory2 == "-") {
            showToast(requireContext(), "카테고리를 선택해주세요.")
            return
        }

        localVideosCount = imageFileNames.filterNotNull().count()

        if (localVideosCount == 0)
            saveDataWithoutImages()
        else {
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
            }
        }
    }

    private fun saveDataWithoutImages() {
        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {
            val categories = mutableListOf<String?>()
            val pr = PrModel()
            val user = MainActivity.currentUser!!

            pr.publisherId = user.id
            pr.publisherName = user.publicName
            pr.occupation = "a" // must be imp.
            pr.title = edit_text_title.text.toString()
            pr.content = (edit_text_description.text ?: "").toString()
            pr.registrationTime =
                SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            pr.imageNames = imageFileNames
            pr.thumbnailUri = thumbnailUri
            pr.youtubeVideoIds = youtubeVideoIds

            if (selectedCategory1 != "-")
                categories.add(categoryDocumentNames.getValue(selectedCategory1))

            if (selectedCategory2 != "-")
                categories.add(categoryDocumentNames.getValue(selectedCategory2))

            pr.categories = categories

            val hashCode = hashString(user.id + pr.registrationTime).chunked(16)[0]
            FirebaseFirestore.getInstance()
                .collection(COLLECTION_PR_LIST)
                .document(hashCode)
                .set(pr)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showToast(requireContext(), "PR이 등록되었습니다.", Toast.LENGTH_LONG)
                        clearUI()
                        button_upload.isEnabled = true
                    }
                    else {
                        showToast(requireContext(), "등록에 실패했습니다.", Toast.LENGTH_LONG)
                        println("$TAG: ${task.exception}")
                        button_upload.isEnabled = true
                    }
                }
        }
    }

    private fun uploadImage(uri: Uri, fileName: String) {
        val storageReference =
            FirebaseStorage.getInstance().reference
                .child(COLLECTION_IMAGES)
                .child(MainActivity.currentUser!!.id)
                .child(fileName)

        storageReference.putFile(uri).continueWithTask {
            return@continueWithTask storageReference.downloadUrl
        }.addOnSuccessListener {
            println("$TAG: image uploaded, uri: $it")
            ++savedLocalVideosCount

            if (localVideosCount == savedLocalVideosCount)
                saveDataWithoutImages()
        }
    }

    private fun clearUI() {
        editTextClearWithFadeOut(edit_text_title)
        editTextClearWithFadeOut(edit_text_description)
        spinner_categories_1.setSelection(0)
        imageViewCrossFadeOutChain()
    }

    private fun editTextClearWithFadeOut(editText: EditText) {
        val animationIn = AlphaAnimation(0.0F, 1.0F)
        val animationOut = AlphaAnimation(1.0F, 0.0F)

        animationIn.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        animationOut.duration = animationIn.duration

        animationOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                editText.text.clear()
                editText.startAnimation(animationIn)
            }

            override fun onAnimationStart(p0: Animation?) {

            }
        })

        editText.startAnimation(animationOut)
    }

    private fun imageViewCrossFadeOutChain() {
        val shortAnimationTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        image_view_add_3.apply {
            animate().alpha(0F)
                .setDuration(shortAnimationTime.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        image_view_add_2.apply {
                            animate().alpha(0F)
                                .setDuration(shortAnimationTime.toLong())
                                .setListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator?) {
                                        Glide.with(image_view_add_1.context)
                                            .load(R.drawable.ic_add_to_photos_grey_128dp)
                                            .error(R.drawable.ic_add_to_photos_grey_128dp)
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true)
                                            .transition(DrawableTransitionOptions.withCrossFade())
                                            .transform(CircleCrop())
                                            .into(image_view_add_1)
                                    }
                                })
                        }
                    }
                })
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

        const val ACTION_FROM_WRITING_FRAGMENT = "action_from_writing_fragment"
        const val KEY_CHANNELS = "key_channels"

        private enum class ImageSource {
            DEVICE,
            YOUTUBE
        }
    }
}