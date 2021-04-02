package com.sandystudios.talky

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.sandystudios.talky.adapters.ChatAdapter
import com.sandystudios.talky.models.*
import com.sandystudios.talky.utils.KeyboardVisibilityUtil
import com.sandystudios.talky.utils.isSameDayAs
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiEditText
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.ios.IosEmojiProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


const val USER_ID = "userId"
const val USER_THUMB_IMAGE = "thumbImage"
const val USER_NAME = "userName"

class ChatActivity : AppCompatActivity() {

    private val rootView: RelativeLayout by lazy {
        findViewById(R.id.rootView)
    }

    private val rvMessages: RecyclerView by lazy {
        findViewById(R.id.rv_messages)
    }

    private val btnEmoji: ImageView by lazy {
        findViewById(R.id.btn_emoji)
    }

    private val etTypeMessage: EmojiEditText by lazy {
        findViewById(R.id.et_type_message)
    }

    private val tvChatName: TextView by lazy {
        findViewById(R.id.tv_chat_name)
    }

    private val ivChatImage: ImageView by lazy {
        findViewById(R.id.iv_chat_image)
    }

    private val friendId by lazy {
        intent.getStringExtra(USER_ID)!!
    }

    private val friendName by lazy {
        intent.getStringExtra(USER_NAME)
    }

    private val friendImage by lazy {
        intent.getStringExtra(USER_THUMB_IMAGE)
    }

    private val mCurrentUid by lazy {
        FirebaseAuth.getInstance().uid!!
    }

    private val db by lazy {
        FirebaseDatabase.getInstance()
    }

    private lateinit var currentUser: User
    private lateinit var chatAdapter: ChatAdapter

    private lateinit var keyboardVisibilityHelper: KeyboardVisibilityUtil
    private val mutableItems: MutableList<ChatEvent> = mutableListOf()
    private val mLinearLayout: LinearLayoutManager by lazy { LinearLayoutManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(IosEmojiProvider())
        setContentView(R.layout.activity_chat)

        keyboardVisibilityHelper = KeyboardVisibilityUtil(rootView) {
            rvMessages.scrollToPosition(mutableItems.size - 1)
        }

        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)!!
            }

        chatAdapter = ChatAdapter(mutableItems, mCurrentUid)

        rvMessages.apply {
            mLinearLayout.stackFromEnd = true
            layoutManager = mLinearLayout
            adapter = chatAdapter
        }

        rvMessages.adapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                rvMessages.smoothScrollToPosition(rvMessages.adapter!!.itemCount - 1)
            }
        })

        tvChatName.text = friendName
        Picasso.get().load(friendImage).into(ivChatImage)

        val emojiPopup = EmojiPopup.Builder.fromRootView(rootView).build(etTypeMessage)
        btnEmoji.setOnClickListener {
            emojiPopup.toggle()
        }

        findViewById<SwipeRefreshLayout>(R.id.swipe_to_load).setOnRefreshListener {
            val workerScope = CoroutineScope(Dispatchers.Main)
            workerScope.launch {
                delay(2000)
                findViewById<SwipeRefreshLayout>(R.id.swipe_to_load).isRefreshing = false
            }
        }

        findViewById<ImageView>(R.id.btn_send).setOnClickListener {
            etTypeMessage.text?.let {
                if (it.isNotEmpty()) {
                    sendMessage(it.toString())
                    it.clear()
                }
            }
//            rvMessages.scrollToPosition(rvMessages.adapter!!.itemCount - 1)
        }
        listenMessages() { msg, update ->
            if (update) {
                updateMessage(msg)
            } else {
                addMessage(msg)
            }
        }

        chatAdapter.highFiveClick = { id, status ->
            updateHighFive(id, status)
        }
        updateReadCount()
    }


    private fun updateReadCount() {
        getInbox(mCurrentUid, friendId).child("count").setValue(0)
    }

    private fun updateHighFive(id: String, status: Boolean) {
        getMessages(friendId).child(id).updateChildren(mapOf("liked" to status))
    }

    private fun addMessage(event: Message) {
        val eventBefore = mutableItems.lastOrNull()

        // Add date header if it's a different day
        if ((eventBefore != null
                    && !eventBefore.sentAt.isSameDayAs(event.sentAt))
            || eventBefore == null
        ) {
            mutableItems.add(
                DateHeader(
                    event.sentAt, this
                )
            )
        }
        mutableItems.add(event)

        chatAdapter.notifyItemInserted(mutableItems.size)
        rvMessages.scrollToPosition(mutableItems.size + 1)
    }

    private fun getId(friendId: String): String {
        return if (friendId > mCurrentUid) {
            mCurrentUid + friendId
        } else {
            friendId + mCurrentUid
        }
    }

    private fun updateMessage(msg: Message) {
        val position = mutableItems.indexOfFirst {
            when (it) {
                is Message -> it.msgId == msg.msgId
                else -> false
            }
        }
        mutableItems[position] = msg

        chatAdapter.notifyItemChanged(position)
    }

    private fun listenMessages(newMsg: (msg: Message, update: Boolean) -> Unit) {
        getMessages(friendId)
            .orderByKey()
            .addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildChanged(data: DataSnapshot, p1: String?) {
                    val msg = data.getValue(Message::class.java)!!
                    newMsg(msg, true)
                }

                override fun onChildAdded(data: DataSnapshot, p1: String?) {
                    val msg = data.getValue(Message::class.java)!!
                    newMsg(msg, false)
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                }
            })
    }

    private fun sendMessage(msg: String) {
        val id = getMessages(friendId).push().key //Unique Key
        checkNotNull(id) { "Cannot be null" }
        val msgMap = Message(msg, mCurrentUid, id)
        getMessages(friendId).child(id).setValue(msgMap)
        updateLastMessage(msgMap, mCurrentUid)
    }

    private fun updateLastMessage(message: Message, mCurrentUid: String) {
        val inboxMap = Inbox(
            message.msg,
            friendId,
            friendName!!,
            friendImage!!,
            message.sentAt,
            0
        )

        getInbox(mCurrentUid, friendId).setValue(inboxMap)

        getInbox(friendId, mCurrentUid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                val value = p0.getValue(Inbox::class.java)
                inboxMap.apply {
                    from = message.senderId
                    name = currentUser.name
                    image = currentUser.thumbImage
                    count = 1
                }
                if (value?.from == message.senderId) {
                    inboxMap.count = value.count + 1
                }
                getInbox(friendId, mCurrentUid).setValue(inboxMap)
            }

        })
    }

    private fun getInbox(toUser: String, fromUser: String) =
        db.reference.child("chats/$toUser/$fromUser")

    private fun getMessages(friendId: String) =
        db.reference.child("messages/${getId(friendId)}")

    override fun onResume() {
        super.onResume()
        rootView.viewTreeObserver
            .addOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }


    override fun onPause() {
        super.onPause()
        rootView.viewTreeObserver
            .removeOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
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