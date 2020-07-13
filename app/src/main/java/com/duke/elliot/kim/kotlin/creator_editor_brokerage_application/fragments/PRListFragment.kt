package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.interfaces.OnSwipeTouchListener
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.PRListAdapter
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.PRModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_pr_list.*

class PRListFragment : Fragment() {

    lateinit var prListAdapter: PRListAdapter
    private lateinit var collectionReference: CollectionReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        collectionReference = FirebaseFirestore.getInstance().collection(PR_LIST)
        return inflater.inflate(R.layout.fragment_pr_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // readPr()
        observePr()

        if (MainActivity.currentUser == null) {
            recycler_view_pr.isLongClickable = true
            recycler_view_pr.setOnTouchListener(
                OnSwipeTouchListener(
                    requireActivity()
                )
            )
        } else {
            recycler_view_pr.isLongClickable = false
            recycler_view_pr.setOnTouchListener(null)
        }
    }

    private fun readPr() {
        collectionReference.get().addOnSuccessListener { querySnapshot ->
                val prModels = querySnapshot.documents.map { PRModel(it.data!!) }
                prListAdapter =
                    PRListAdapter((activity as MainActivity), R.id.frame_layout_fragment_pr_list, prModels.toMutableList())

                recycler_view_pr.apply {
                    setHasFixedSize(true)
                    adapter = prListAdapter
                    layoutManager = LayoutManagerWrapper(context, 1)
                }
            }
    }

    private fun observePr() {
        prListAdapter =
            PRListAdapter((activity as MainActivity), R.id.frame_layout_fragment_pr_list, mutableListOf())

        recycler_view_pr.apply {
            setHasFixedSize(true)
            adapter = prListAdapter
            layoutManager = LayoutManagerWrapper(context, 1)
        }

        collectionReference.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null)
                println("$TAG: $firebaseFirestoreException")
            else {
                for (change in documentSnapshot!!.documentChanges) {
                    when (change.type) {
                        DocumentChange.Type.ADDED -> prListAdapter.insert(PRModel(change.document.data))
                        DocumentChange.Type.MODIFIED -> prListAdapter.update(PRModel(change.document.data))
                        DocumentChange.Type.REMOVED -> prListAdapter.delete(PRModel(change.document.data))
                        else -> {  }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "PRListFragment"
    }

}