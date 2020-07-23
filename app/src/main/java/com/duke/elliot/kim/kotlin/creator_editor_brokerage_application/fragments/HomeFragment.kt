package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.Mode

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.HomeFragmentStateAdapter
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.interfaces.OnSwipeTouchListener
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.setColorFilter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    var selectedTabIndex = 0
    var tabLayout: TabLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        tabLayout = view.tab_layout
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPagerAndTabLayout()
        //view_pager.isUserInputEnabled = false
    }

    private fun initViewPagerAndTabLayout() {
        view_pager.adapter = HomeFragmentStateAdapter(requireActivity())

        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            tab.tag = position
            tab.text = tabTexts[position]
            tab.setIcon(tabIcons[position])
            tab.icon!!.setColorFilter(
                ContextCompat.getColor(
                    requireContext(), R.color.colorTabIconUnselected), Mode.SRC_IN)
        }.attach()

        val linearLayout = tab_layout.getChildAt(0) as LinearLayout
        for (i in 0 until linearLayout.childCount) {
            linearLayout.getChildAt(i).setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    tab_layout.getTabAt(i)?.select()
                }
                true
            }
        }

        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.icon?.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(), R.color.colorTabIconUnselected), Mode.SRC_IN)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTabIndex = tab?.tag as Int
                println("WHATISPROB  $selectedTabIndex")

                tab.icon?.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(), R.color.colorTabIconSelected), Mode.SRC_IN)

                view_pager.isUserInputEnabled = selectedTabIndex != PARTNERS_TAB_INDEX
            }
        })
    }

    companion object {
        const val PARTNERS_TAB_INDEX = 1
        const val PR_LIST_TAB_INDEX = 0

        private val tabIcons = arrayOf(
            R.drawable.ic_pr_list_32dp,
            R.drawable.ic_people_32dp
        )

        private val tabTexts = arrayOf("PR 리스트", "파트너스")
    }
}
