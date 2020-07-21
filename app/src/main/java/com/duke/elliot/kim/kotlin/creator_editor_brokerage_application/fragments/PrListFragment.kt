package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.interfaces.OnSwipeTouchListener
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_IMAGES
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_PR_LIST
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.PrModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_pr_list.*
import kotlinx.android.synthetic.main.item_view_pr.view.*

class PrListFragment : Fragment() {

    private lateinit var collectionReference: CollectionReference
    private lateinit var listenerRegistration: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        collectionReference = FirebaseFirestore.getInstance().collection(COLLECTION_PR_LIST)
        return inflater.inflate(R.layout.fragment_pr_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        if (FirebaseAuth.getInstance().currentUser == null) {
            recycler_view_pr_list.isLongClickable = true
            recycler_view_pr_list.setOnTouchListener(
                OnSwipeTouchListener(requireActivity())
            )
        } else {
            recycler_view_pr_list.isLongClickable = false
            recycler_view_pr_list.setOnTouchListener(null)
        }
    }

    private fun initRecyclerView() {
        recycler_view_pr_list.apply {
            setHasFixedSize(true)
            adapter = PrListRecyclerViewAdapter()
            layoutManager = LayoutManagerWrapper(context, 1)
        }
    }

    inner class PrListRecyclerViewAdapter : RecyclerView.Adapter<PrListRecyclerViewAdapter.ViewHolder>() {

        private val prList = mutableListOf<PrModel>()

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        init {
            setPrSnapshotListener(this)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PrListRecyclerViewAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_pr, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return prList.size
        }

        override fun onBindViewHolder(holder: PrListRecyclerViewAdapter.ViewHolder, position: Int) {
            val pr = prList[position]
            holder.view.text_view_title.text = pr.title
            // holder.view.text_view_publisher = prList[position].get
            @Suppress("UNCHECKED_CAST")
            loadImage(holder.view.image_view_thumbnail,
                pr.publisherId, pr.imageNames as List<String?>)

            holder.view.setOnClickListener {
                val prFragment = PrFragment()
                prFragment.setPr(pr)
                (activity as MainActivity).startFragment(prFragment, R.id.frame_layout_fragment_pr_list, MainActivity.PR_FRAGMENT_TAG)
            }
        }

        private fun loadImage(imageView: ImageView, userId: String, imageNames: List<String?>) {
            val storageReference = FirebaseStorage.getInstance().reference

            if (imageNames[0] != null)
                storageReference.child(COLLECTION_IMAGES)
                    .child(userId).child(imageNames[0]!!).downloadUrl
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Glide.with(imageView.context)
                                .load(task.result)
                                .error(R.drawable.ic_chat_64dp)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .transform(CircleCrop())
                                .into(imageView)
                        } else {
                            println("$TAG: ${task.exception}")
                        }
                    }
        }

        fun insert(pr: PrModel) {
            prList.add(0, pr)
            notifyItemInserted(0)
        }

        fun update(pr: PrModel) {
            notifyItemChanged(getPosition(pr))
        }

        fun delete(pr: PrModel) {
            prList.remove(pr)
            notifyItemRemoved(getPosition(pr))
        }

        private fun getPosition(pr: PrModel) = prList.indexOf(pr)
    }

    private fun setPrSnapshotListener(prListRecyclerViewAdapter: PrListRecyclerViewAdapter) {
        listenerRegistration = collectionReference.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null)
                println("$TAG: $firebaseFirestoreException")
            else {
                for (change in documentSnapshot!!.documentChanges) {
                    when (change.type) {
                        DocumentChange.Type.ADDED -> prListRecyclerViewAdapter.insert(PrModel(change.document.data))
                        DocumentChange.Type.MODIFIED -> prListRecyclerViewAdapter.update(PrModel(change.document.data))
                        DocumentChange.Type.REMOVED -> prListRecyclerViewAdapter.delete(PrModel(change.document.data))
                        else -> { println("$TAG: Unexpected DocumentChange Type") }
                    }
                }
            }
        }
    }

    fun removePrSnapshotListener() {
        if (::listenerRegistration.isInitialized)
            listenerRegistration.remove()
    }

    companion object {
        const val TAG = "PRListFragment"
    }

}