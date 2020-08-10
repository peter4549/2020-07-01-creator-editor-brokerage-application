package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.app.Dialog
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
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.ErrorHandler
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_PR_LIST
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.PrModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.VideoModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_pr_list.view.*
import kotlinx.android.synthetic.main.item_view_pr.view.*
import java.lang.Exception

class PrListFragment : Fragment() {

    private lateinit var listenerRegistration: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pr_list, container, false)
        initRecyclerView(view.recycler_view)
        return view
    }

    private fun initRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            setHasFixedSize(true)
            adapter = PrListRecyclerViewAdapter((requireActivity() as MainActivity))
            layoutManager = LayoutManagerWrapper(context, 1)
        }
    }

    inner class PrListRecyclerViewAdapter(private val activity: MainActivity, isMyPrList: Boolean = false, dialog: Dialog? = null)
        : RecyclerView.Adapter<PrListRecyclerViewAdapter.ViewHolder>() {

        private var collectionReference: CollectionReference =
            FirebaseFirestore.getInstance().collection(COLLECTION_PR_LIST)
        private var dialog: Dialog? = null
        private var prList = mutableListOf<PrModel>()

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        init {
            if (isMyPrList) {
                this.dialog = dialog
                setMyPrList()
            }
            else
                setPrListSnapshotListener(this)
        }

        private fun setMyPrList() {
            collectionReference.whereEqualTo(PrModel.KEY_PUBLISHER_ID, MainActivity.currentUser?.id)
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result != null)
                            if (task.result?.documents == null) {
                                ErrorHandler.errorHandling(
                                    requireContext(),
                                    Exception("pr list not found"), getString(R.string.registered_pr_not_found)
                                )
                            } else {
                                for (document in task.result?.documents!!) {
                                    @Suppress("UNCHECKED_CAST")
                                    val map = document.data as Map<String, Any>
                                    prList.add(PrModel(map))
                                }
                                notifyDataSetChanged()
                            }
                        else
                            ErrorHandler.errorHandling(
                                requireContext(),
                                Exception("pr list not found (task.result is null)"), getString(R.string.registered_pr_not_found))
                    } else {
                        ErrorHandler.errorHandling(
                            requireContext(),
                            task.exception, getString(R.string.registered_pr_not_found))
                    }
                }
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

            if (pr.youtubeVideos[0] == null)
                holder.view.image_view_thumbnail.visibility = View.GONE
            else {
                val video = VideoModel(pr.youtubeVideos[0]!!)
                loadImage(holder.view.image_view_thumbnail, video.thumbnailUri)
            }

            holder.view.setOnClickListener {
                if (dialog != null)
                    dialog!!.dismiss()
                val prFragment = PrFragment()
                prFragment.setPr(pr)
                activity.startFragment(prFragment, R.id.relative_layout_activity_main, MainActivity.PR_FRAGMENT_TAG)
            }
        }

        private fun loadImage(imageView: ImageView, thumbnailUri: String?) {
            if (thumbnailUri != null) {
                when {
                    thumbnailUri.isNotBlank() -> Glide.with(imageView.context)
                        .load(thumbnailUri)
                        .error(R.drawable.ic_sentiment_dissatisfied_grey_24dp)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .transform(CenterCrop(), RoundedCorners(8))
                        .into(imageView)
                    else -> imageView.visibility = View.GONE
                }
            } else
                imageView.visibility = View.GONE
        }

        private fun insert(pr: PrModel) {
            prList.add(0, pr)
            notifyItemInserted(0)
        }

        private fun update(pr: PrModel) {
            notifyItemChanged(getPosition(pr))
        }

        private fun delete(pr: PrModel) {
            prList.remove(pr)
            notifyItemRemoved(getPosition(pr))
        }

        private fun getPosition(pr: PrModel) = prList.indexOf(pr)

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
    }

    fun removePrSnapshotListener() {
        if (::listenerRegistration.isInitialized)
            listenerRegistration.remove()
    }

    companion object {
        const val TAG = "PrListFragment"
    }

}