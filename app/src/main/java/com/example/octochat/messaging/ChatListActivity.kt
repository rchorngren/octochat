package com.example.octochat.messaging

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.ProgressBar
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        listViewChats = findViewById(R.id.listView)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        progressBar = findViewById(R.id.progressBar)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        listViewChats.setOnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", listChats[i].chatId)
            intent.putExtra("otherUserDisplayName", listChats[i].otherUser.displayName)
            intent.putExtra("otherUserUid", listChats[i].otherUser.userId)
            startActivity(intent)
        }
    }

    fun getActiveChats(){
        progressBar.visibility = ProgressBar.VISIBLE
        db.collection("chats")
            .whereArrayContains("users", currentUser!!.uid)
            .get()
            .addOnCompleteListener {

                Log.e(TAG, it.result!!.documents[0].id)

                for(document in it.result!!.documents){
//                    val listUsers = document.get("users")
                    val users = document["users"] as MutableList<String>?
                    var otherUserUid: String? = null
                    for(user in users!!){
                        otherUserUid = if(user == currentUser!!.uid) continue else user
                    }

                    db.collection("users").document(otherUserUid!!)
                        .get()
                        .addOnCompleteListener {
                            val otherUser = it.result!!.toObject(User::class.java)!!
                            listChats.add(Chat(document.id, otherUser))
                        }
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