package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.ChatFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.MyInfoFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.PRListFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.WritingFragment
import java.lang.Exception

class PagerFragmentStateAdapter(fragmentActivity: FragmentActivity)
    : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return PAGE_COUNT
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PRListFragment()
            1 -> WritingFragment()
            2 -> ChatFragment()
            3 -> MyInfoFragment()
            else -> throw Exception("Invalid fragment")
        }
    }

    companion object {
        const val PAGE_COUNT = 4
    }
}