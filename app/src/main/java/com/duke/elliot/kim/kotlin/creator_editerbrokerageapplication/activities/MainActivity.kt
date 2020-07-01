package com.duke.elliot.kim.kotlin.creator_editerbrokerageapplication.activities

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.duke.elliot.kim.kotlin.creator_editerbrokerageapplication.Mode
import com.duke.elliot.kim.kotlin.creator_editerbrokerageapplication.R
import com.duke.elliot.kim.kotlin.creator_editerbrokerageapplication.adapters.PagerFragmentStateAdapter
import com.duke.elliot.kim.kotlin.creator_editerbrokerageapplication.setColorFilter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : FragmentActivity() {

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Instantiate a ViewPager and a PagerAdapter.
        viewPager = findViewById(R.id.view_pager)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = PagerFragmentStateAdapter(this)
        viewPager.adapter = pagerAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTexts[position]
            tab.setIcon(tabIcons[position])
            tab.icon!!.setColorFilter(
                ContextCompat.getColor(
                    this@MainActivity, R.color.colorTabIconUnselected), Mode.SRC_IN)
        }.attach()
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                    tab!!.icon!!.setColorFilter(
                        ContextCompat.getColor(
                            this@MainActivity, R.color.colorTabIconUnselected), Mode.SRC_IN)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab!!.icon!!.setColorFilter(
                    ContextCompat.getColor(
                        this@MainActivity, R.color.colorTabIconSelected), Mode.SRC_IN)
            }

        })
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    companion object {
        private val tabIcons = arrayOf(
            R.drawable.ic_tab_home_24dp,
            R.drawable.ic_tab_edit_24dp,
            R.drawable.ic_tab_chat_24dp,
            R.drawable.ic_tab_person_24dp
        )

        private val tabTexts = arrayOf("홈", "글쓰기", "채팅", "내정보")
    }
}
