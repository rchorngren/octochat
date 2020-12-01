package com.example.octochat.messaging.util

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.octochat.R
import com.example.octochat.messaging.Chat
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

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

        val profileImage = listChats[p0].otherUser.profileImage
        val displayName = listChats[p0].otherUser.displayName
        val lastMessage = if(listChats[p0].lastMessage != null) listChats[p0].lastMessage!!.text else "No messages yet"

        if(profileImage != null && profileImage.isNotEmpty()){
            Picasso.get()
                .load(profileImage)
                .resize(48, 48)
                .into(profilePictureView, object : Callback {
                    override fun onSuccess() {
                        Log.d("ChatListAdapter-picasso", "success")
                    }
                    override fun onError(e: Exception?) {
                        Log.d("ChatListAdapter-picasso", "error")
                    }
                })
        } else {
            profilePictureView.setImageResource(R.drawable.bg_no_pfp)
        }
        displayNameView.text = displayName
        lastTextView.text = lastMessage

        return view
    }
}