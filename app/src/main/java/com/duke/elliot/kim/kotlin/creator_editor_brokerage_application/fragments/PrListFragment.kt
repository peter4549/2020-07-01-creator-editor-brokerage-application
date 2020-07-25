package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_IMAGES
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_PR_LIST
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.PrModel
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

    private fun initRecyclerView() {
        recycler_view.apply {
            setHasFixedSize(true)
            adapter = PrListRecyclerViewAdapter()
            layoutManager = LayoutManagerWrapper(context, 1)
        }
    }

    inner class PrListRecyclerViewAdapter : RecyclerView.Adapter<PrListRecyclerViewAdapter.ViewHolder>() {

        private val prList = mutableListOf<PrModel>()

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        init {
            setPrListSnapshotListener(this)
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
                (activity as MainActivity).startFragment(prFragment, R.id.relative_layout_activity_main, MainActivity.PR_FRAGMENT_TAG)
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
                                .transform(CenterCrop(), RoundedCorners(16))
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

    private fun setPrListSnapshotListener(prListRecyclerViewAdapter: PrListRecyclerViewAdapter) {
        listenerRegistration = collectionReference.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null)
                println("$TAG: $firebaseFirestoreException")
            else {
                for (change in documentSnapshot!!.documentChanges) {
                    when (change.type) {
                        DocumentChange.Type.ADDED -> prListRecyclerViewAdapter.insert(PrModel(change.document.data))
                        DocumentChange.Type.MODIFIED -> prListRecyclerViewAdapter.update(PrModel(change.document.data))
                        DocumentChange.Type.REMOVED -> prListRecyclerViewAdapter.delete(PrModel(change.document.data))
                        else -> { println("$TAG: unexpected DocumentChange Type") }
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
        const val TAG = "PrListFragment"
    }

}