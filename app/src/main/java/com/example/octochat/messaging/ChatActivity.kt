package com.example.octochat.messaging

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.octochat.R
import com.example.octochat.messaging.util.MessagesListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*

class ChatActivity : AppCompatActivity() {

    val TAG = "ChatActivity"
    var user: FirebaseUser? = null
    val listMessages = mutableListOf<Message>()

    lateinit var textFullName: TextView
    lateinit var messagesList: ListView
    lateinit var messageAdapter: MessagesListAdapter

    lateinit var messagesRef: CollectionReference

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    lateinit var email: String
    lateinit var password: String
    lateinit var otherUserEmail: String
    lateinit var username: String
    lateinit var displayName: String

    lateinit var otherUserUid: String
    lateinit var otherUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.hide()

        //Intent
//        email = intent.getStringExtra("email")!!
//        password = intent.getStringExtra("password")!!
//        otherUserEmail = intent.getStringExtra("otherUserEmail")!!
//        username = intent.getStringExtra("username")!!
//        displayName = intent.getStringExtra("displayName")!!
        val chatId = intent.getStringExtra("chatId")!!
        otherUserUid = intent.getStringExtra("otherUserUid")!!
        val otherUserDisplayName = intent.getStringExtra("otherUserDisplayName")


        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        messagesRef = db.collection("chats").document(chatId).collection("messages")

        textFullName = findViewById(R.id.textFullName)
        messagesList = findViewById(R.id.listViewMessages)
        val backIcon = findViewById<ImageView>(R.id.iconBack)
        val moreIcon = findViewById<ImageView>(R.id.iconMore)
        val sendTestMessageButton = findViewById<ImageView>(R.id.buttonSendTestMessage)
        val editText = findViewById<EditText>(R.id.textField)
        val sendButton = findViewById<ImageView>(R.id.buttonSend)

        textFullName.text = otherUserDisplayName

        createChat()

        sendButton.setOnClickListener {
            Log.e(TAG, user!!.uid)
            messagesRef.add(Message(user!!.uid, editText.text.toString()))
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        messageAdapter.notifyDataSetChanged()
                        messagesList.smoothScrollToPosition(listMessages.size - 1)
                    }
                }
            editText.setText("")
        }
    }


    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e(TAG, "Successful login")

                user = auth.currentUser
                saveUserToFirestore()

                //Replaced in ChatListActivity
                db.collection("chats")
                    .whereArrayContains("users", user!!.uid)
                    .get()
                    .addOnCompleteListener {

                        Log.e(TAG, it.result!!.documents[0].id)

                        if (it.result!!.documents.size > 0) {
                            createChat()
                            messagesRef = db.collection("chats").document(it.result!!.documents[0].id).collection("messages")

                        } else {
                            Log.e(TAG, "hittar ingen dig i chatter")
                            createChat(true)
                        }
                    }
            } else {
                Log.e(TAG, "Login failed; ${it.exception}")
            }
        }
    }

    fun setSnapshotListener() {
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "error: $error")
                    return@addSnapshotListener
                }

                for (message in snapshot!!.documentChanges) {
                    Log.e(TAG, "${message.type}")

                    if (message.type == DocumentChange.Type.MODIFIED) return@addSnapshotListener
                    val newDocument = message.document.toObject(Message::class.java)
                    listMessages.add(newDocument)
                }
                messageAdapter.notifyDataSetChanged()
                messagesList.smoothScrollToPosition(listMessages.size - 1)
            }
    }

    fun saveUserToFirestore() {
        db.collection("users").document(user!!.uid)
            .set(User(user!!.uid, email, username, displayName))
    }

    override fun onDestroy() {
        auth.signOut()
        super.onDestroy()
    }

    fun createChat(new: Boolean = false) {
        val currentUser = User(user!!.uid, email, username, displayName)

        db.collection("users")
            .document(otherUserUid)
            .get()
            .addOnCompleteListener {
                otherUser = it.result!!.toObject(User::class.java)!!

//                textFullName.text = otherUser.displayName

//                if(new){
//                    db.collection("chats").document().set(hashMapOf("users" to listOf(user!!.uid, otherUser.userId)) as Map<String, Any>)
//                }
                messageAdapter = MessagesListAdapter(this, listMessages, currentUser, otherUser)
                messagesList.adapter = messageAdapter
                setSnapshotListener()
            }
    }
}