package com.sandystudios.talky

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        val spannable = SpannableString("Talky.")
        spannable.setSpan(
            ForegroundColorSpan(getColor(R.color.primaryBlack)),
            5, // start
            6, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        findViewById<TextView>(R.id.tv_AppName).text = spannable
    }
}