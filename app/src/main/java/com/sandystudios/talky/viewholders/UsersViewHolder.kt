package com.sandystudios.talky.viewholders

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.sandystudios.talky.R
import com.sandystudios.talky.models.User
import com.squareup.picasso.Picasso

class UsersViewHolder(itemView:View): RecyclerView.ViewHolder(itemView) {

    fun bind(user: User, onClick: (name: String, photo: String, id: String) -> Unit) =
        with(itemView) {
            findViewById<TextView>(R.id.tv_count).isVisible = false
            findViewById<TextView>(R.id.tv_time).isVisible = false

            findViewById<TextView>(R.id.tv_title).text = user.name
            findViewById<TextView>(R.id.tv_subtitle).text = user.about

            val ivUserImage = findViewById<ShapeableImageView>(R.id.iv_user_image)
            Log.d("DP", user.imageUrl)
            Picasso.get()
                .load(user.imageUrl)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(ivUserImage)

            setOnClickListener {
                onClick.invoke(user.name, user.thumbImage, user.uid)
            }
        }
}