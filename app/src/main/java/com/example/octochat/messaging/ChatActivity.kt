package com.example.octochat.messaging

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.octochat.R
import com.example.octochat.UserProfile
import com.example.octochat.messaging.util.MessagesListAdapter
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.attatchment_dialog.*

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

        sendButton.setOnClickListener {
            messagesRef.add(Message(currentUserFb!!.uid, editText.text.toString()))
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        messageAdapter.notifyDataSetChanged()
                        messagesList.smoothScrollToPosition(listMessages.size - 1)
                    } else {
                        Log.e(TAG, it.exception.toString())
                    }
                }

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
                    Log.e(TAG, "${message.type}")

                    if (message.type == DocumentChange.Type.MODIFIED) return@addSnapshotListener
                    val newDocument = message.document.toObject(Message::class.java)
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
// Added by Jaya to show the menu list

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                true
            }
            R.id.menu_editprofile -> {
                val intent = Intent(this, UserProfile::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}