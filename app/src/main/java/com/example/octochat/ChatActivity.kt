package com.example.octochat

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.octochat.util.MessagesListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*

class ChatActivity : AppCompatActivity() {

    val TAG = "ChatActivity"
    var user: FirebaseUser? = null
    val listMessages = mutableListOf<Message>()
    val listUsers = mutableListOf<User>()

    lateinit var textFullName: TextView
    lateinit var messagesList: ListView
    lateinit var messageAdapter: MessagesListAdapter

    lateinit var listenerRegistration: ListenerRegistration
    lateinit var messagesRef: CollectionReference
    lateinit var usersRef: CollectionReference

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    lateinit var email: String
    lateinit var password: String
    lateinit var otherUserEmail: String
    lateinit var username: String
    lateinit var displayName: String

    lateinit var otherUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.hide()

        email = intent.getStringExtra("email")!!
        password = intent.getStringExtra("password")!!
        otherUserEmail = intent.getStringExtra("otherUserEmail")!!
        username = intent.getStringExtra("username")!!
        displayName = intent.getStringExtra("displayName")!!

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        loginUser(email, password)

        messagesRef = db.collection("chats")
        usersRef = db.collection("chats").document("1").collection("participants")

        textFullName = findViewById(R.id.textFullName)
        messagesList = findViewById(R.id.listViewMessages)
        val backIcon = findViewById<ImageView>(R.id.iconBack)
        val moreIcon = findViewById<ImageView>(R.id.iconMore)
        val sendTestMessageButton = findViewById<ImageView>(R.id.buttonSendTestMessage)
        val editText = findViewById<EditText>(R.id.textField)
        val sendButton = findViewById<ImageView>(R.id.buttonSend)

        sendTestMessageButton.setOnClickListener {

        }

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
        listenerRegistration = messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
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

        //get list of users in a chat. Maybe better for group chat?
        usersRef.addSnapshotListener { snapshot, error ->
            for (participant in snapshot!!.documents) {
                val newDocument = participant.toObject(User::class.java)
                if (newDocument != null) listUsers.add(newDocument)
            }
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
            .whereEqualTo("email", otherUserEmail)
            .get()
            .addOnCompleteListener {
                otherUser = it.result!!.documents[0].toObject(User::class.java)!!

                textFullName.text = otherUser.displayName

                if(new){
                    db.collection("chats").document().set(hashMapOf("users" to listOf(user!!.uid, otherUser.userId)) as Map<String, Any>)
                }
                messageAdapter = MessagesListAdapter(this, listMessages, currentUser, otherUser)
                messagesList.adapter = messageAdapter
                setSnapshotListener()
            }
    }
}