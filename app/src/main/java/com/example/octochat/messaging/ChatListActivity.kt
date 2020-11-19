package com.example.octochat.messaging

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginStart
import com.example.octochat.EditProfile
import com.example.octochat.R
import com.example.octochat.SettingsActivity
import com.example.octochat.UserProfile
import com.example.octochat.messaging.util.ChatListAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

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
            otherUserEmailField.hint = "Enter email of recipient"

            dialogBuilder.setView(otherUserEmailField)
            dialogBuilder.setTitle("Start chat")

            dialogBuilder.setPositiveButton("Start") { dialogInterface, i ->

                val otherUserEmail = otherUserEmailField.text.toString()
                if (otherUserEmail != "") {
                    db.collection("users")
                        .whereEqualTo("email", otherUserEmail)
                        .get()
                        .addOnCompleteListener {
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
                                            ), "timestamp" to FieldValue.serverTimestamp()
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
            .addSnapshotListener { snapshot, error ->
                if (snapshot!!.documents.size > 0) {
                    snapshot.documents.forEachIndexed { index, document ->

                        if (listChats.size > 0) {
                            listChats.clear()
                        }

                        val timestamp = document["timestamp"] as Timestamp?
                        val timestampDate: Date
                        if (timestamp != null) timestampDate =
                            timestamp.toDate() else return@forEachIndexed

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

                                    db.collection("chats")
                                        .document(document.id)
                                        .collection("messages")
                                        .orderBy("timestamp", Query.Direction.ASCENDING)
                                        .limitToLast(1)
                                        .get()
                                        .addOnCompleteListener {
                                            var addChat = true
                                            for (chat in listChats) {
                                                if (chat.chatId == document.id) {
                                                    addChat = false
                                                    break
                                                }
                                            }
                                            if (addChat) {
                                                if (it.result!!.documents.size > 0) {
                                                    val message = it.result!!.documents[0].toObject(Message::class.java)!!

                                                    if (message.sender == auth.currentUser!!.uid) { //if you sent the message
                                                        message.text = "You: " + message.text
                                                        listChats.add(Chat(document.id, otherUser, message, timestampDate))
                                                    } else {
                                                        listChats.add(Chat(document.id, otherUser, message, timestampDate))
                                                    }
                                                    listChats.sortByDescending { it.timestamp!!.time }
                                                } else {
                                                    listChats.add(Chat(document.id, otherUser))
                                                }
                                            }
                                            chatListAdapter.notifyDataSetChanged()
                                        }
                                }
                            }
                    }
                } else {
                    if(listChats.size > 0) listChats.clear()
                    progressBar.visibility = ProgressBar.GONE
                    emptyView.visibility = TextView.VISIBLE
                }
                chatListAdapter = ChatListAdapter(this, listChats)
                listViewChats.adapter = chatListAdapter
            }
    }

    // adding menu button for the user profile screen - Jaya

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_editprofile -> {
                val intent = Intent(this, UserProfile::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_chats -> {
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}