package com.example.octochat.messaging.util

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.octochat.R
import com.example.octochat.messaging.FriendsListActivity
import com.example.octochat.messaging.User
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class FriendsListAdapter(val context: Context, val listUsers: MutableList<User>) : BaseAdapter(){

    val inflater = LayoutInflater.from(context)

    override fun getCount() = listUsers.size

    override fun getItem(p0: Int) = listUsers[p0]

    override fun getItemId(p0: Int) = p0.toLong()

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view = inflater.inflate(R.layout.list_item_friend, p2, false)

        val navProfilePic = view.findViewById<ImageView>(R.id.imageFriendPfp)
        val nameText = view.findViewById<TextView>(R.id.textFriendName)
        val userNameText = view.findViewById<TextView>(R.id.textFriendUsername)
        val startChatButton = view.findViewById<ImageView>(R.id.buttonStartChat)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        val profileImage = listUsers[p0].profileImage
        val name = listUsers[p0].displayName
        val username = "@" + listUsers[p0].username

        if(profileImage != null && profileImage.isNotEmpty()){
            Picasso.get()
                .load(profileImage)
                .resize(48, 48)
                .into(navProfilePic, object : Callback {
                    override fun onSuccess() {
                        Log.d("FriendsListAdapter", "success")
                    }
                    override fun onError(e: Exception?) {
                        Log.d("FriendsListAdapter", "error")
                    }
                })
        } else {
            navProfilePic.setImageResource(R.drawable.bg_no_pfp)
        }

        nameText.text = name
        userNameText.text = username
        
        startChatButton.setOnClickListener {
            (context as FriendsListActivity).startChat(p0)
            startChatButton.visibility = ImageView.GONE
            progressBar.visibility = ProgressBar.VISIBLE
        }


        return view
    }

}