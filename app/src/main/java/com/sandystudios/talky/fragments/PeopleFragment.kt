package com.sandystudios.talky.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sandystudios.talky.ChatActivity
import com.sandystudios.talky.R
import com.sandystudios.talky.models.User
import com.sandystudios.talky.viewholders.DeletedUserViewHolder
import com.sandystudios.talky.viewholders.UsersViewHolder
import java.lang.Exception

private const val DELETED_VIEW_TYPE = 1
private const val NORMAL_VIEW_TYPE = 2

class PeopleFragment : Fragment() {

    private lateinit var mAdapter: FirestorePagingAdapter<User, RecyclerView.ViewHolder>

    private val db by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name", Query.Direction.ASCENDING)
    }

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setUpAdapter()
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.rv_main).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }
    }

    private fun setUpAdapter() {
        // Init Paging Configuration
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(2)
            .setPageSize(12)
            .build()

        // Init Adapter Configuration
        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(this)
            .setQuery(db, config, User::class.java)
            .build()

        mAdapter = object : FirestorePagingAdapter<User, RecyclerView.ViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val inflater = layoutInflater
                return when (viewType) {
                    NORMAL_VIEW_TYPE -> {
                        UsersViewHolder(inflater.inflate(R.layout.list_item_users, parent, false))
                    }
                    else -> DeletedUserViewHolder(
                        inflater.inflate(
                            R.layout.empty_item_view,
                            parent,
                            false
                        )
                    )

                }
            }

            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                user: User
            ) {
                if (holder is UsersViewHolder)
                    if (auth.uid == user.uid) {
                        currentList?.snapshot()?.removeAt(position)
                        notifyItemRemoved(position)
                    } else {
                        holder.bind(user) { name: String, photo: String, id: String ->
                            startActivity(
                                ChatActivity.createChatActivity(
                                    requireContext(),
                                    id,
                                    name,
                                    photo
                                )
                            )
                        }
                    }
            }

            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(User::class.java)
                return if (auth.uid == item!!.uid) {
                    DELETED_VIEW_TYPE
                } else {
                    NORMAL_VIEW_TYPE
                }
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                when (state) {
                    LoadingState.ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            "Error Occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    LoadingState.LOADING_INITIAL -> {}
                    LoadingState.LOADING_MORE -> {}
                    LoadingState.LOADED -> {}
                    LoadingState.FINISHED -> {}
                }
            }

            override fun onError(e: Exception) {
                super.onError(e)
                e.message?.let { Log.e("MainActivity", it) }
            }
        }
    }
}