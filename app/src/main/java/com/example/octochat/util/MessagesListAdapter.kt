package com.example.octochat.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.octochat.Message
import com.example.octochat.R
import com.example.octochat.User
import com.google.firebase.firestore.FirebaseFirestore

class MessagesListAdapter(val context: Context, val listMessages: MutableList<Message>, val currentUser: User?, val otherUser: User?) :
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

        val message = listMessages[p0].text

        var displayName = "unknown"
        var userId: String? = null

        if (listMessages[p0].sender == currentUser!!.userId) {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
                marginEnd = 12
            }
            displayName = currentUser.displayName!!
            userId = currentUser.userId

            displayNameView.layoutParams = params
            messageView.background.setTint(ResourcesCompat.getColor(context.resources, R.color.colorMessageBackgroundGray, null))
//            displayNameView.visibility = TextView.GONE
            messageView.layoutParams = params
        } else {
            displayName = otherUser!!.displayName!!
            userId = otherUser.userId
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