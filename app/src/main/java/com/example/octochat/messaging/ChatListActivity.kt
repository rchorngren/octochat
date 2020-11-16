package com.example.octochat.messaging

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.octochat.R
import com.example.octochat.messaging.util.ChatListAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

//Väldigt osäker om det här ens funkar, måste ha en databas med användare och ganska specifika regler för det
class ChatListActivity : AppCompatActivity() {

    val TAG = "ChatListActivity"
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    var currentUser: FirebaseUser? = null
    val listChats = mutableListOf<Chat>()
    lateinit var chatListAdapter: ChatListAdapter

    lateinit var listViewChats: ListView
    lateinit var progressBar: ProgressBar
    lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        listViewChats = findViewById(R.id.listView)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        getActiveChats()

        fab.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val otherUserEmailField = EditText(this)

            dialogBuilder.setView(otherUserEmailField)
            dialogBuilder.setTitle("Start chat")

            dialogBuilder.setPositiveButton("Start") { dialogInterface, i ->

                val otherUserEmail = otherUserEmailField.text.toString()
                if (otherUserEmail != "") {
                    db.collection("users")
                        .whereEqualTo("email", otherUserEmail)
                        .get()
                        .addOnCompleteListener {
//                            Log.e("ChatListActivity", it.result!!.documents.toString())
                            if (it.result!!.documents.size > 0) {
                                val otherUser =
                                    it.result!!.documents[0].toObject(User::class.java)!!

                                db.collection("chats")
                                    .document()
                                    .set(
                                        hashMapOf(
                                            "users" to listOf(
                                                auth.currentUser!!.uid,
                                                otherUser.userId
                                            )
                                        )
                                    )

                                getActiveChats()
                            } else Log.e(
                                "ChatListActivity",
                                "No user with email $otherUserEmail found"
                            )
                        }
                }
            }.show()
        }

        listViewChats.setOnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", listChats[i].chatId)
            intent.putExtra("otherUserDisplayName", listChats[i].otherUser.displayName)
            intent.putExtra("otherUserUid", listChats[i].otherUser.userId)
            startActivity(intent)
        }
    }

    fun getActiveChats() {
        progressBar.visibility = ProgressBar.VISIBLE
        emptyView.visibility = TextView.GONE

        db.collection("chats")
            .whereArrayContains("users", auth.currentUser!!.uid)
            .get()
            .addOnCompleteListener {

                Log.e(TAG, it.result!!.documents.size.toString())

                if (it.result!!.documents.size > 0) {
                    for (document in it.result!!.documents) {
                        val users = document["users"] as MutableList<String>?
                        var otherUserUid: String? = null

                        for (user in users!!) {
                            otherUserUid = if (user == auth.currentUser!!.uid) continue else user
                        }

                        db.collection("users").document(otherUserUid!!)
                            .get()
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    progressBar.visibility = ProgressBar.GONE
                                    val otherUser = it.result!!.toObject(User::class.java)!!
                                    listChats.add(Chat(document.id, otherUser))
                                    chatListAdapter.notifyDataSetChanged()
                                }
                            }
                    }
                } else {
                    progressBar.visibility = ProgressBar.GONE
                    emptyView.visibility = TextView.VISIBLE
                }

                chatListAdapter = ChatListAdapter(this, listChats)
                listViewChats.adapter = chatListAdapter

//                if (it.result!!.documents.size > 0) {
//                    //Start chat activity with existing chat
//                    //messagesRef = db.collection("chats").document(it.result!!.documents[0].id).collection("messages")
//
//                } else {
//                    Log.e(TAG, "hittar ingen dig i chatter")
//                    //Start chat activity and add a new chat into the database
//                }
            }
    }
}