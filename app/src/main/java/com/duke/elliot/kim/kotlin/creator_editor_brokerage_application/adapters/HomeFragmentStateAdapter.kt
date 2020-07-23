package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity

class HomeFragmentStateAdapter(private val fragmentActivity: FragmentActivity)
    : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = PAGE_COUNT

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> (fragmentActivity as MainActivity).prListFragment
            1 -> (fragmentActivity as MainActivity).partnersFragment
            else -> throw Exception("invalid fragment")
        }
    }

    companion object {
        const val PAGE_COUNT = 2
    }
}