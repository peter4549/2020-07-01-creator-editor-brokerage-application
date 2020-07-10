package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.OnSwipeTouchListener
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (MainActivity.currentUser == null) {
            recycler_view_pr.isLongClickable = true
            recycler_view_pr.setOnTouchListener(OnSwipeTouchListener(requireActivity()))
        } else {
            recycler_view_pr.isLongClickable = false
            recycler_view_pr.setOnTouchListener(null)
        }
    }

    private fun readPr() {
        FirebaseFirestore.getInstance()
            .collection(PR_LIST)
            .get().addOnSuccessListener { querySnapshot ->
                val map = querySnapshot.documents.map { it.data!! }
                recyclerViewAdapter = RecyclerViewAdapter(map)

                recycler_view_pr.apply {
                    setHasFixedSize(true)
                    adapter = recyclerViewAdapter
                    layoutManager = LayoutManagerWrapper(context, 1)
                }
            }
    }
}