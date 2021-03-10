package com.sandystudios.talky

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class OtpActivity : AppCompatActivity() {

    private val tvOtpAppName: TextView by lazy {
        findViewById<TextView>(R.id.tv_otp_app_name)
    }

    private val tvOtpSentTo: TextView by lazy {
        findViewById<TextView>(R.id.tv_otp_sent_to)
    }

    private val etOtp: EditText by lazy {
        findViewById<EditText>(R.id.et_otp)
    }

    private val tvResendOtp: TextView by lazy {
        findViewById<TextView>(R.id.tv_resend_otp)
    }

    private val btnVerify: Button by lazy {
        findViewById<Button>(R.id.btn_verify)
    }

    private lateinit var countryCode: String
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        val spannable = SpannableString("Talky.")
        spannable.setSpan(
            ForegroundColorSpan(getColor(R.color.primaryBlue)),
            5, // start
            6, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        tvOtpAppName.text = spannable

        val otpSentToText = getString(R.string.otp_sent_to)
        val spannableOtpSentTo = SpannableString(otpSentToText)
        spannableOtpSentTo.setSpan(
            ForegroundColorSpan(getColor(R.color.primaryBlue)),
            22, // start
            36, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        tvOtpSentTo.text = spannableOtpSentTo

        val str = "RESEND OTP"
        makeTextLink(tvResendOtp, str, true, getColor(R.color.primaryBlue), action = {
            Toast.makeText(
                this,
                "Clicked",
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    private fun makeTextLink(textView: TextView, str: String, underlined: Boolean, color: Int?, action: (() -> Unit)? = null) {
        val spannableString = SpannableString(textView.text)
        val textColor = color ?: textView.currentTextColor
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                action?.invoke()
            }
            override fun updateDrawState(drawState: TextPaint) {
                super.updateDrawState(drawState)
                drawState.isUnderlineText = underlined
                drawState.color = textColor
            }
        }
        val index = spannableString.indexOf(str)
        spannableString.setSpan(clickableSpan, index, index + str.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }
}