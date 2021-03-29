package com.sandystudios.talky

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

const val USER_ID = "userId"
const val USER_THUMB_IMAGE = "thumbImage"
const val USER_NAME = "userName"

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
    }

    companion object {

        fun createChatActivity(context: Context, id: String, name: String, image: String): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(USER_ID, id)
            intent.putExtra(USER_NAME, name)
            intent.putExtra(USER_THUMB_IMAGE, image)

            return intent
        }
    }
}