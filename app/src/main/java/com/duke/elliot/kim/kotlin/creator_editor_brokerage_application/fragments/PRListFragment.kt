package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.RecyclerViewAdapter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_pr_list.*

class PRListFragment : Fragment() {

    lateinit var recyclerViewAdapter: RecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        readPr()
        return inflater.inflate(R.layout.fragment_pr_list, container, false)
    }

    private fun readPr() {
        FirebaseFirestore.getInstance()
            .collection(PR_LIST)
            .get().addOnSuccessListener { querySnapshot ->
                val map = querySnapshot.documents.map { it.data!! }
                recyclerViewAdapter = RecyclerViewAdapter(map)

                recycler_view.apply {
                    setHasFixedSize(true)
                    adapter = recyclerViewAdapter
                    layoutManager = LayoutManagerWrapper(context, 1)
                }
            }
    }
}