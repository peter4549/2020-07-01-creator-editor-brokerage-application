package com.duke.elliot.kim.kotlin.creator_editerbrokerageapplication.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.duke.elliot.kim.kotlin.creator_editerbrokerageapplication.fragments.PRListFragment
import java.lang.Exception

class PagerFragmentStateAdapter(fragmentActivity: FragmentActivity)
    : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {

    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PRListFragment()
            else -> throw Exception("Invalid fragment")
        }
    }
}