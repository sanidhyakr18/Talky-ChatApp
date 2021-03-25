package com.sandystudios.talky

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.sandystudios.talky.models.User
import com.sandystudios.talky.models.defaultImageUrl

class ProfileSetupActivity : AppCompatActivity(), View.OnClickListener {

    private val userImgView: ShapeableImageView by lazy {
        findViewById(R.id.userImgView)
    }

    private val btnEditImage: MaterialButton by lazy {
        findViewById(R.id.btn_edit_image)
    }

    private val etName: EditText by lazy {
        findViewById(R.id.et_name)
    }

    private val etAbout: EditText by lazy {
        findViewById(R.id.et_about)
    }

    private val btnComplete: Button by lazy {
        findViewById(R.id.btn_complete)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var downloadUrl: String

    private val database by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)
        auth = Firebase.auth
        btnEditImage.setOnClickListener(this)
        btnComplete.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            btnEditImage -> {
                checkPermissionForImage()
            }
            btnComplete -> {
                completeSetup()
            }
        }
    }

    private fun completeSetup() {
        val name = etName.text.toString()
        val about = etAbout.text.toString()
        if (name.isEmpty()) {
            Toast.makeText(this, "Name can not be empty!", Toast.LENGTH_SHORT).show()
        } else if (about.isEmpty()) {
            Toast.makeText(this, "About can not be empty!", Toast.LENGTH_SHORT).show()
        } else {
            btnComplete.isEnabled = false
            btnEditImage.isClickable = false
            val user = if (!::downloadUrl.isInitialized) {
                User(name, auth.uid!!, about)
            } else {
                User(name, downloadUrl, downloadUrl/*Needs to thumbnail url*/, auth.uid!!, about)
            }
            database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "An error occurred. Please try again later", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

                requestPermissions(
                    permission,
                    1001
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
            }
            openGallery()
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK && requestCode == 1001) {
            intent?.data?.let { image ->
                userImgView.setImageURI(image)
                uploadImage(image)
            }
        }
    }

    private fun uploadImage(image: Uri) {
        val storageRef =
            FirebaseStorage.getInstance().reference.child("uploads/${auth.uid.toString()}")
        val uploadTask = storageRef.putFile(image)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                //Handle error
                Log.e("Error uploading", task.exception.toString())
            }
            return@Continuation storageRef.downloadUrl
        }).addOnCompleteListener { task ->
//            btnComplete.isEnabled = true
            if (task.isSuccessful) {
                Log.i("Done uploading", task.result.toString())
                downloadUrl = task.result.toString()
            }
            Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_SHORT)
                .show()
        }.addOnFailureListener {
//            btnComplete.isEnabled = true
            Toast.makeText(this, "Upload failed! Check network connection.", Toast.LENGTH_SHORT)
                .show()
        }
    }
}