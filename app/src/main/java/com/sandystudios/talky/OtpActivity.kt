package com.sandystudios.talky

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity(), View.OnClickListener {

    private val tvOtpAppName: TextView by lazy {
        findViewById(R.id.tv_otp_app_name)
    }

    private val tvOtpSentTo: TextView by lazy {
        findViewById(R.id.tv_otp_sent_to)
    }

    private val etOtp: EditText by lazy {
        findViewById(R.id.et_otp)
    }

    private val tvResendOtp: TextView by lazy {
        findViewById(R.id.tv_resend_otp)
    }

    private val btnVerify: Button by lazy {
        findViewById(R.id.btn_verify)
    }

    private val tvDetail: TextView by lazy {
        findViewById(R.id.tvDetail)
    }

    private lateinit var phoneNumber: String


    private lateinit var auth: FirebaseAuth
    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        btnVerify.setOnClickListener(this)
        etOtp.addTextChangedListener {
            btnVerify.isEnabled = !(it.isNullOrEmpty() || it.length < 6)
        }

        try {
            phoneNumber = intent.getStringExtra(PHONE_NUMBER)!!
            val otpSentToText =
                getText(R.string.otp_sent_to).toString() + " " + phoneNumber.substring(
                    0,
                    3
                ) + " " + phoneNumber.substring(3)
            tvOtpSentTo.text = otpSentToText
        } catch (e: Exception) {
            Toast.makeText(this, "Number not found, Try Again!", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }

        setSpannableTextViews()

        auth = Firebase.auth

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d("TAG", "onVerificationCompleted:$credential")

                btnVerify.isEnabled = false
                tvResendOtp.isEnabled = false
                // [START_EXCLUDE silent]
                verificationInProgress = false
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential)
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                // [START_EXCLUDE silent]
                verificationInProgress = false
                // [END_EXCLUDE]

                if (e is FirebaseAuthInvalidCredentialsException) {
                    AlertDialog.Builder(this@OtpActivity)
                        .setTitle("Invalid phone number!")
                        .setMessage("Kindly re-enter your phone number.")
                        .setPositiveButton(
                            "OK"
                        ) { _, _ ->
                            startActivity(
                                Intent(
                                    this@OtpActivity,
                                    LoginActivity::class.java
                                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                        }
                        .setCancelable(false)
                        .show()
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(
                        findViewById(android.R.id.content), "Quota exceeded.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED)
                // [END_EXCLUDE]
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token

                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT)
                // [END_EXCLUDE]
            }
        }

        init()
    }

//    override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//        updateUI(currentUser)
//
//        // [START_EXCLUDE]
//        if (verificationInProgress) {
//            startPhoneNumberVerification(phoneNumber)
//        }
//        // [END_EXCLUDE]
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    // [START_EXCLUDE]
                    updateUI(STATE_SIGNIN_SUCCESS, user)
                    // [END_EXCLUDE]
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        // [START_EXCLUDE silent]
//                        etOtp.error = "Invalid code."
                        // [END_EXCLUDE]

                        AlertDialog.Builder(this@OtpActivity)
                            .setTitle("Invalid OTP!")
                            .setMessage("Please re-enter OTP.")
                            .setCancelable(true)
                            .show()
                    }
                    // [START_EXCLUDE silent]
                    // Update UI
                    updateUI(STATE_SIGNIN_FAILED)
                    // [END_EXCLUDE]
                }
            }
    }

    private fun setSpannableTextViews() {
        val spannable = SpannableString("Talky.")
        spannable.setSpan(
            ForegroundColorSpan(getColor(R.color.primaryBlue)),
            5, // start
            6, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        tvOtpAppName.text = spannable

//        "Enter the OTP sent to +91 8527303983"
        val otpSentToText = tvOtpSentTo.text
        val spannableOtpSentTo = SpannableString(otpSentToText)
        spannableOtpSentTo.setSpan(
            ForegroundColorSpan(getColor(R.color.primaryBlue)),
            22, // start
            36, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        tvOtpSentTo.text = spannableOtpSentTo

        val str = "RESEND OTP"
        makeTextLink(tvResendOtp, str, true, getColor(R.color.primaryBlue))
    }

    private fun makeTextLink(
        textView: TextView,
        str: String,
        underlined: Boolean,
        color: Int?
    ) {
        val spannableString = SpannableString(textView.text)
        val textColor = color ?: textView.currentTextColor
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                resendVerificationCode(phoneNumber, resendToken)
            }

            override fun updateDrawState(drawState: TextPaint) {
                super.updateDrawState(drawState)
                drawState.isUnderlineText = underlined
                drawState.color = textColor
            }
        }
        val index = spannableString.indexOf(str)
        spannableString.setSpan(
            clickableSpan,
            index,
            index + str.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }

    private fun init() {
        startPhoneNumberVerification(phoneNumber)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user)
        } else {
            updateUI(STATE_INITIALIZED)
        }
    }

    private fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    private fun updateUI(
        uiState: Int,
        user: FirebaseUser? = auth.currentUser,
        cred: PhoneAuthCredential? = null
    ) {
        when (uiState) {
            STATE_INITIALIZED -> {
                // Initialized state, show only the phone number field and start button
            }
            STATE_CODE_SENT -> {
                // Code sent state, show the verification field
                tvDetail.text = "Code Sent!"
            }
            STATE_VERIFY_FAILED -> {
                // Verification has failed, show all options
                tvDetail.text = "OTP verification failed!"
            }
            STATE_VERIFY_SUCCESS -> {
                // Verification has succeeded, proceed to firebase sign in
                tvDetail.text = "Verification succeeded!"

                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.smsCode != null) {
                        etOtp.setText(cred.smsCode)
                    } else {
                        etOtp.setText(R.string.instant_validation)
                    }
                }
                Log.d(TAG, "updateUI: STATE_VERIFY_SUCCESS")
                showProfileSetupActivity()
            }
            STATE_SIGNIN_FAILED ->
                // No-op, handled by sign-in check
                tvDetail.text = "Sign-in failed!"
            STATE_SIGNIN_SUCCESS -> {
                Log.d(TAG, "updateUI: STATE_SIGNIN_SUCCESS")
                showProfileSetupActivity()
            }
        } // Np-op, handled by sign-in check

    }

    private fun showProfileSetupActivity() {
        startActivity(Intent(this, ProfileSetupActivity::class.java))
        finish()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_verify -> {
                val code = etOtp.text.toString()
                verifyPhoneNumberWithCode(storedVerificationId, code)
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        // [END start_phone_auth]

        verificationInProgress = true
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
        if (token != null) {
            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    companion object {
        private const val TAG = "PhoneAuthActivity"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }
}