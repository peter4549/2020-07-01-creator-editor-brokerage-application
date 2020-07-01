package com.duke.elliot.kim.kotlin.creator_editerbrokerageapplication

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.duke.elliot.kim.kotlin.creator_editerbrokerageapplication.adapters.PagerFragmentStateAdapter

class MainActivity : FragmentActivity() {

    private lateinit var pager: ViewPager2
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Instantiate a ViewPager and a PagerAdapter.
        pager = findViewById(R.id.view_pager)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = PagerFragmentStateAdapter(this)
        pager.adapter = pagerAdapter
    }

    override fun onBackPressed() {
        if (pager.currentItem == 0) {
            super.onBackPressed()
        } else {
            pager.currentItem = pager.currentItem - 1
        }
    }
}
