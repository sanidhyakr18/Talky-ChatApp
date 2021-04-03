package com.sandystudios.talky.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.sandystudios.talky.R
import com.sandystudios.talky.models.Inbox
import com.sandystudios.talky.utils.formatAsListItem
import com.squareup.picasso.Picasso

class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: Inbox, onClick: (name: String, photo: String, id: String) -> Unit) =
        with(itemView) {
            findViewById<TextView>(R.id.tv_count).apply {
                isVisible = item.count > 0
                text = item.count.toString()
            }

            findViewById<TextView>(R.id.tv_time).text = item.time.formatAsListItem(context)
            findViewById<TextView>(R.id.tv_title).text = item.name
            findViewById<TextView>(R.id.tv_subtitle).text = item.msg
            Picasso.get()
                .load(item.image)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(findViewById<ImageView>(R.id.iv_user_image))
            setOnClickListener {
                onClick.invoke(item.name, item.image, item.from)
            }
        }
}