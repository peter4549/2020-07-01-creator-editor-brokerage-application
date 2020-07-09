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
        text_view_test.setOnTouchListener(OnSwipeTouchListener(requireActivity()))

        if (MainActivity.currentUser == null) {
            linear_layout_fragment_pr_list.isLongClickable = true
            linear_layout_fragment_pr_list.setOnTouchListener(OnSwipeTouchListener(requireActivity()))
        } else {
            linear_layout_fragment_pr_list.isLongClickable = false
            linear_layout_fragment_pr_list.setOnTouchListener(null)
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