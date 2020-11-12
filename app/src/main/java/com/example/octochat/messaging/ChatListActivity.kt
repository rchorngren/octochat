package com.example.octochat.messaging

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.ProgressBar
import com.example.octochat.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ChatListActivity : AppCompatActivity() {

    val TAG = "ChatListActivity"
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    var currentUser: FirebaseUser? = null
    val listChats = mutableListOf<Chat>()

    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        val list = findViewById<ListView>(R.id.listView)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        progressBar = findViewById(R.id.progressBar)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

    }

    fun getActiveChats(){
        progressBar.visibility = ProgressBar.VISIBLE
        db.collection("chats")
            .whereArrayContains("users", currentUser!!.uid)
            .get()
            .addOnCompleteListener {

                Log.e(TAG, it.result!!.documents[0].id)

                for(document in it.result!!.documents){



                }

                if (it.result!!.documents.size > 0) {
                    //Start chat activity with existing chat
                    //messagesRef = db.collection("chats").document(it.result!!.documents[0].id).collection("messages")

                } else {
                    Log.e(TAG, "hittar ingen dig i chatter")
                    //Start chat activity and add a new chat into the database
                }
            }
    }



}