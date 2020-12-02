package com.example.octochat.messaging.util

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.octochat.messaging.Message
import com.example.octochat.R
import com.example.octochat.messaging.Chat
import com.example.octochat.messaging.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class MessagesListAdapterGroupChat(val context: Context, val listMessages: MutableList<Message>, val currentUser: User, val listUsers: MutableList<User>) :
    BaseAdapter() {
    val inflater = LayoutInflater.from(context)
    val db = FirebaseFirestore.getInstance()
    val usersRef = db.collection("chats").document("1").collection("participants")

    override fun getCount() = listMessages.size

    override fun getItem(p0: Int) = listMessages[p0]

    override fun getItemId(p0: Int) = p0.toLong()

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view = inflater.inflate(R.layout.list_item_message, p2, false)

        val displayNameView = view.findViewById<TextView>(R.id.textViewDisplayName)
        val messageView = view.findViewById<TextView>(R.id.textViewMessage)

        var displayName = "unknown"
        val userId = listMessages[p0].sender

        for(user in listUsers){
            if (user.userId == listMessages[p0].sender) {
                displayName = user.displayName!!
                break
            }
        }

        val message = listMessages[p0].text

        if (listMessages[p0].sender == currentUser.userId) {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
                marginEnd = 12
            }
            displayNameView.layoutParams = params
            messageView.background.setTint(ResourcesCompat.getColor(context.resources, R.color.colorMessageBackgroundGray, null))
            displayNameView.visibility = TextView.GONE
            messageView.layoutParams = params
        }

        displayNameView.text = displayName
        messageView.text = message

        if (p0 > 0) {
            if (listMessages[p0-1].sender == userId) {
                displayNameView.visibility = TextView.GONE
            }
        }
        return view
    }
}