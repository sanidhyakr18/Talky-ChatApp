package com.sandystudios.talky.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.sandystudios.talky.R
import com.sandystudios.talky.models.ChatEvent
import com.sandystudios.talky.models.DateHeader
import com.sandystudios.talky.models.Message
import com.sandystudios.talky.utils.formatAsTime

class ChatAdapter(private val list: MutableList<ChatEvent>, private val mCurrentUser: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var highFiveClick: ((id: String, status: Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate = { layout: Int ->
            LayoutInflater.from(parent.context)
                .inflate(layout, parent, false)
        }
        return when (viewType) {
            TEXT_MESSAGE_RECEIVED -> {
                MessageHolder(
                    inflate(R.layout.list_item_chat_received)
                )
            }
            TEXT_MESSAGE_SENT -> {
                MessageHolder(
                    inflate(R.layout.list_item_chat_sent)
                )
            }
            DATE_HEADER -> {
                DateHeaderHolder(
                    inflate(R.layout.list_item_date_header)
                )
            }
            else -> {
                MessageHolder(
                    inflate(R.layout.list_item_chat_received)
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {

        return when (val event = list[position]) {
            is Message -> {
                if (event.senderId == mCurrentUser) {
                    TEXT_MESSAGE_SENT
                } else {
                    TEXT_MESSAGE_RECEIVED
                }
            }
            is DateHeader -> DATE_HEADER
            else -> UNSUPPORTED
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = list[position]) {
            is DateHeader -> {
                holder.itemView.findViewById<TextView>(R.id.tv_date_header).text = item.date
            }
            is Message -> {
                holder.itemView.findViewById<TextView>(R.id.tv_message_content).text = item.msg
                holder.itemView.findViewById<TextView>(R.id.tv_message_time).text = item.sentAt.formatAsTime()
                when (getItemViewType(position)) {
                    TEXT_MESSAGE_RECEIVED -> {
                        holder.itemView.findViewById<CardView>(R.id.cv_received_message).setOnClickListener(object :
                            DoubleClickListener() {
                            override fun onDoubleClick(v: View?) {
                                highFiveClick?.invoke(item.msgId, !item.liked)
                            }
                        })
                        holder.itemView.findViewById<ImageView>(R.id.high_five_received).apply {
                            isVisible = position == itemCount - 1 || item.liked
                            isSelected = item.liked
                            setOnClickListener {
                                highFiveClick?.invoke(item.msgId, !isSelected)
                            }
                        }
                    }

                    TEXT_MESSAGE_SENT -> {
                        holder.itemView.findViewById<ImageView>(R.id.high_five_sent).apply {
                            isVisible = item.liked
                        }
                    }
                }
            }
        }
    }

    class DateHeaderHolder(view: View) : RecyclerView.ViewHolder(view)

    class MessageHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        private const val UNSUPPORTED = -1
        private const val TEXT_MESSAGE_RECEIVED = 0
        private const val TEXT_MESSAGE_SENT = 1
        private const val DATE_HEADER = 2
    }

}

abstract class DoubleClickListener : View.OnClickListener {
    private var lastClickTime: Long = 0
    override fun onClick(v: View?) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
            lastClickTime = 0
        }
//        else {
//            onSingleClick(v)
//        }
        lastClickTime = clickTime
    }

    //    abstract fun onSingleClick(v: View?)
    abstract fun onDoubleClick(v: View?)

    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
    }
}