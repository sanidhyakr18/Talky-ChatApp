package com.sandystudios.talky

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sandystudios.talky.adapters.ScreenSlidePagerAdapter
import com.sandystudios.talky.models.User
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private val mainToolbar: MaterialToolbar by lazy {
        findViewById(R.id.toolbar_main)
    }

    private val viewPager: ViewPager2 by lazy {
        findViewById(R.id.vp_main)
    }

    private val tabs: TabLayout by lazy {
        findViewById(R.id.tl_main)
    }

//    private val mCurrentUid: String by lazy {
//        FirebaseAuth.getInstance().currentUser!!.uid
//    }
//
//    private var currentUser = User("", "", "", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(mainToolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        viewPager.adapter = ScreenSlidePagerAdapter(this)
        TabLayoutMediator(
            tabs, viewPager
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

//        Load DP
//        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
//            .addOnSuccessListener {
//                currentUser = it.toObject(User::class.java)!!
//                Log.e("DP", "Success: ${currentUser.thumbImage}")
//            }
//
//        val ivUserMain = findViewById<ShapeableImageView>(R.id.iv_user_main)
//
//        Picasso.get()
//            .load(currentUser.thumbImage)
//            .placeholder(R.drawable.default_avatar)
//            .error(R.drawable.default_avatar)
//            .into(ivUserMain)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView
        item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                // TODO: 27-03-2021 write code
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                // TODO: 27-03-2021 write code
                return true
            }

        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    // TODO: 27-03-2021 write code
                }
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }
}