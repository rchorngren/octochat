package com.example.octochat.messaging.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.octochat.R
import com.example.octochat.messaging.FriendsListActivity
import com.example.octochat.messaging.User

class FriendsListAdapter(val context: Context, val listUsers: MutableList<User>) : BaseAdapter(){

    val inflater = LayoutInflater.from(context)

    override fun getCount() = listUsers.size

    override fun getItem(p0: Int) = listUsers[p0]

    override fun getItemId(p0: Int) = p0.toLong()

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view = inflater.inflate(R.layout.list_item_friend, p2, false)

        val pfpView = view.findViewById<ImageView>(R.id.imageFriendPfp)
        val nameText = view.findViewById<TextView>(R.id.textFriendName)
        val userNameText = view.findViewById<TextView>(R.id.textFriendUsername)
        val startChatButton = view.findViewById<ImageView>(R.id.buttonStartChat)

        startChatButton.setOnClickListener {
            (context as FriendsListActivity).startChat(p0)
        }

        val name = listUsers[p0].displayName
        val username = "@" + listUsers[p0].username

        nameText.text = name
        userNameText.text = username

        return view
    }

}