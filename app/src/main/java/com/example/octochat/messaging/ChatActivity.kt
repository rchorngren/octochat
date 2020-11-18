package com.example.octochat.messaging

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.octochat.EditProfile
import com.example.octochat.R
import com.example.octochat.UserProfile
import com.example.octochat.messaging.util.MessagesListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import java.util.zip.Inflater

class ChatActivity : AppCompatActivity() {

    val TAG = "ChatActivity"
    var currentUserFb: FirebaseUser? = null
    val listMessages = mutableListOf<Message>()

    lateinit var textFullName: TextView
    lateinit var messagesList: ListView
    lateinit var messageAdapter: MessagesListAdapter

    lateinit var messagesRef: CollectionReference

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    lateinit var otherUserUid: String
    lateinit var otherUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.hide()

        //Intent
        val chatId = intent.getStringExtra("chatId")!!
        otherUserUid = intent.getStringExtra("otherUserUid")!!
        val otherUserDisplayName = intent.getStringExtra("otherUserDisplayName")

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserFb = auth.currentUser

        messagesRef = db.collection("chats").document(chatId).collection("messages")

        textFullName = findViewById(R.id.textFullName)
        messagesList = findViewById(R.id.listViewMessages)
        val backIcon = findViewById<ImageView>(R.id.iconBack)
        val moreIcon = findViewById<ImageView>(R.id.iconMore)
        val sendAttachmentButton = findViewById<ImageView>(R.id.buttonSendAttachment)
        val editText = findViewById<EditText>(R.id.textField)
        val sendButton = findViewById<ImageView>(R.id.buttonSend)

        textFullName.text = otherUserDisplayName

        createChat()

        backIcon.setOnClickListener { finish() }

        registerForContextMenu(moreIcon)
        moreIcon.setOnClickListener { openContextMenu(moreIcon) }

        sendButton.setOnClickListener {
            val message = Message(currentUserFb!!.uid, editText.text.toString())
            messagesRef.add(message)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        messageAdapter.notifyDataSetChanged()
                        messagesList.smoothScrollToPosition(listMessages.size - 1)
                    } else {
                        Log.e(TAG, it.exception.toString())
                    }
                }

            db.collection("chats").document(chatId).set(hashMapOf("timestamp" to FieldValue.serverTimestamp()), SetOptions.merge())

            editText.setText("")
        }

        editText.setOnClickListener {
                messagesList.smoothScrollToPosition(listMessages.size-1)
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
                    if (message.type == DocumentChange.Type.MODIFIED) return@addSnapshotListener
                    val newDocument = message.document.toObject(Message::class.java)

                    //this line adds the latest message to the conversation, maybe check if
                    //the user is currently in the app and if they sent it, and if they aren't, send a notification?
                    listMessages.add(newDocument)

                }
                messageAdapter.notifyDataSetChanged()
                messagesList.smoothScrollToPosition(listMessages.size - 1)
            }
    }

    fun createChat() {
        var currentUser: User?
        db.collection("users").document(currentUserFb!!.uid).get().addOnCompleteListener {
            if (it.isSuccessful){
                val currentUserAttributes = it.result!!.toObject(User::class.java)!!
                val email = currentUserAttributes.email!!
                val username = currentUserAttributes.username!!
                val displayName = currentUserAttributes.displayName!!
                currentUser = User(currentUserFb!!.uid, email, username, displayName)

                getMessages(currentUser!!)
            } else Log.e("ChatActivity", it.exception.toString())
        }
    }
    fun getMessages(currentUser: User){
        db.collection("users")
            .document(otherUserUid)
            .get()
            .addOnCompleteListener {
                otherUser = it.result!!.toObject(User::class.java)!!

                messageAdapter = MessagesListAdapter(this, listMessages, currentUser, otherUser)
                messagesList.adapter = messageAdapter
                setSnapshotListener()
            }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_chat, menu)

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.profile ->{
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.block -> {
                Toast.makeText(this, "Block", Toast.LENGTH_SHORT).show()
                true
                }
            else -> super.onContextItemSelected(item)
        }
    }
}