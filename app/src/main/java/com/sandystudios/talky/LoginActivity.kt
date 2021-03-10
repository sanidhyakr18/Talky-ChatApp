package com.sandystudios.talky

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.hbb20.CountryCodePicker

const val PHONE_NUMBER = "phone"

class LoginActivity : AppCompatActivity() {

    private val tvLoginAppName: TextView by lazy {
        findViewById<TextView>(R.id.tv_login_app_name)
    }

    private val tvOtp: TextView by lazy {
        findViewById<TextView>(R.id.tv_otp)
    }

    private val etPhoneNumber: EditText by lazy {
        findViewById<EditText>(R.id.et_phone_number)
    }

    private val btnNext: Button by lazy {
        findViewById<Button>(R.id.btn_next)
    }

    private lateinit var countryCode: String
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val spannable = SpannableString("Talky.")
        spannable.setSpan(
            ForegroundColorSpan(getColor(R.color.primaryBlue)),
            5, // start
            6, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        tvLoginAppName.text = spannable

        val spannableOtp = SpannableString("We will send you One Time Password (OTP)")
        spannableOtp.setSpan(
            ForegroundColorSpan(getColor(R.color.primaryBlue)),
            17, // start
            40, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        tvOtp.text = spannableOtp

        etPhoneNumber.addTextChangedListener {
            btnNext.isEnabled = !(it.isNullOrEmpty() || it.length < 10)
        }

        btnNext.setOnClickListener {
            checkNumber()
        }
    }

    private fun checkNumber() {
        countryCode = findViewById<CountryCodePicker>(R.id.ccp).selectedCountryCodeWithPlus
        phoneNumber = countryCode + etPhoneNumber.text.toString()

        //Add some validation here
        startActivity(Intent(this, OtpActivity::class.java).putExtra(PHONE_NUMBER, phoneNumber))
    }
}