package com.example.octochat.messaging.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.octochat.R
import com.example.octochat.messaging.Chat

class ChatListAdapter(val context: Context, val listChats: MutableList<Chat>) : BaseAdapter(){

    val inflater = LayoutInflater.from(context)

    override fun getCount() = listChats.size

    override fun getItem(p0: Int) = listChats[p0]

    override fun getItemId(p0: Int) = p0.toLong()

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view = inflater.inflate(R.layout.list_item_chat, p2, false)

        val profilePictureView = view.findViewById<ImageView>(R.id.imageViewProfilePicture)
        val displayNameView = view.findViewById<TextView>(R.id.textViewDisplayName)
        val lastTextView = view.findViewById<TextView>(R.id.textViewLastMessage)

        val displayName = listChats[p0].otherUser.displayName
        val lastMessage = if(listChats[p0].lastMessage != null) listChats[p0].lastMessage!!.text else "No messages yet"

        displayNameView.text = displayName

        lastTextView.text = lastMessage

        return view
    }
}