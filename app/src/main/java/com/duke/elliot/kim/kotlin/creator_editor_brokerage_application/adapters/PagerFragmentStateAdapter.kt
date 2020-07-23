package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.HomeFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.MyInfoFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.WritingFragment
import java.lang.Exception

class PagerFragmentStateAdapter(private val fragmentActivity: FragmentActivity)
    : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = PAGE_COUNT

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> (fragmentActivity as MainActivity).homeFragment
            1 -> WritingFragment()
            2 -> (fragmentActivity as MainActivity).chatRoomsFragment
            3 -> MyInfoFragment()
            else -> throw Exception("invalid fragment")
        }
    }

    companion object {
        const val PAGE_COUNT = 4
    }
}