package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.interfaces.OnSwipeTouchListener
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_partners.*

class PartnersFragment : Fragment() {

    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_partners, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        test_id.isLongClickable = true
        test_id.setOnTouchListener(OnSwipeTouchListener(requireActivity()))
    }

    override fun onResume() {
        super.onResume()

        // 액티비티서 해제해주던지..


        /*
        if (FirebaseAuth.getInstance().currentUser == null) {

        } else {

        }

         */
    }

    override fun onStop() {
        super.onStop()
    }
}
