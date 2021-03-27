package com.sandystudios.talky

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sandystudios.talky.adapters.ScreenSlidePagerAdapter

class MainActivity : AppCompatActivity() {

    private val mainToolbar: MaterialToolbar by lazy {
        findViewById(R.id.main_toolbar)
    }

    private val viewPager: ViewPager2 by lazy {
        findViewById(R.id.viewPager)
    }

    private val tabs: TabLayout by lazy {
        findViewById(R.id.tabs)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(mainToolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        viewPager.adapter = ScreenSlidePagerAdapter(this)
        TabLayoutMediator(tabs, viewPager
        ) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "CHATS"
                }
                1 -> {
                    tab.text = "PEOPLE"
                }
            }
        }.attach()

        val spannable = SpannableString("Talky.")
        spannable.setSpan(
            ForegroundColorSpan(getColor(R.color.primaryBlue)),
            5, // start
            6, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        findViewById<TextView>(R.id.tv_main_app_name).text = spannable
    }
}