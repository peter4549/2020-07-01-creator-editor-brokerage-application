package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_PARTNERS
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.interfaces.OnSwipeTouchListener
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.PartnerModel
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_partners.*
import kotlinx.android.synthetic.main.fragment_partners.view.*
import kotlinx.android.synthetic.main.item_view_partner.view.*
import kotlinx.android.synthetic.main.item_view_partner.view.image_view_profile

class PartnersFragment : Fragment() {

    private lateinit var collectionReference: CollectionReference
    private lateinit var listenerRegistration: ListenerRegistration
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_partners, container, false)

        collectionReference = FirebaseFirestore.getInstance().collection(COLLECTION_PARTNERS)
        initRecyclerView(view.recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_view.isLongClickable = true
        recycler_view.setOnTouchListener(OnSwipeTouchListener(requireActivity()))
    }

    private fun initRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            setHasFixedSize(true)
            adapter = PartnersRecyclerViewAdapter()
            layoutManager = LayoutManagerWrapper(context, 1)
        }
    }

    inner class PartnersRecyclerViewAdapter : RecyclerView.Adapter<PartnersRecyclerViewAdapter.ViewHolder>() {

        private val partners = mutableListOf<PartnerModel>()

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val imageView = view.image_view_profile!!
            val textViewPublicName = view.text_view_public_name!!
            val textViewOccupation = view.text_view_occupation!!
            val textViewStatusMessage = view.text_view_status_message!!
        }

        init {
            setPartnersSnapshotListener(this)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PartnersRecyclerViewAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_partner, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return partners.size
        }

        override fun onBindViewHolder(holder: PartnersRecyclerViewAdapter.ViewHolder, position: Int) {
            val partner = partners[position]

            if (partner.profileImageUri.isNotBlank())
                loadProfileImage(holder.imageView, partner.profileImageUri)

            holder.textViewPublicName.text = partner.publicName
            holder.textViewStatusMessage.text = partner.statusMessage
            holder.textViewOccupation.text = partner.occupation
        }

        private fun loadProfileImage(imageView: ImageView, profileImageFileUri: String) {
            Glide.with(imageView.context)
                .load(profileImageFileUri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.ic_chat_64dp)
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(CircleCrop())
                .into(imageView)
        }

        fun insert(partner: PartnerModel) {
            partners.add(0, partner)
            notifyItemInserted(0)
        }

        fun update(partner: PartnerModel) {
            notifyItemChanged(getPosition(partner))
        }

        fun delete(partner: PartnerModel) {
            partners.remove(partner)
            notifyItemRemoved(getPosition(partner))
        }

        private fun getPosition(partner: PartnerModel) = partners.indexOf(partner)
    }

    private fun setPartnersSnapshotListener(partnersRecyclerViewAdapter: PartnersRecyclerViewAdapter) {
        listenerRegistration = collectionReference.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null)
                println("$TAG: $firebaseFirestoreException")
            else {
                for (change in documentSnapshot!!.documentChanges) {
                    when (change.type) {
                        DocumentChange.Type.ADDED -> partnersRecyclerViewAdapter.insert(PartnerModel(change.document.data))
                        DocumentChange.Type.MODIFIED -> partnersRecyclerViewAdapter.update(PartnerModel(change.document.data))
                        DocumentChange.Type.REMOVED -> partnersRecyclerViewAdapter.delete(PartnerModel(change.document.data))
                        else -> { println("$TAG: unexpected DocumentChange Type") }
                    }
                }
            }
        }
    }

    fun removePartnersSnapshotListener() {
        if (::listenerRegistration.isInitialized)
            listenerRegistration.remove()
    }

    companion object {
        private const val TAG = "PartnersFragment"
    }
}
