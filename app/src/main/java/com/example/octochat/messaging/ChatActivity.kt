package com.example.octochat.messaging

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.octochat.R
import com.example.octochat.messaging.util.MessagesListAdapter
import com.example.octochat.messaging.util.MessagesListAdapterGroupChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ChatActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    val TAG = "ChatActivity"
    var currentUserFb: FirebaseUser? = null
    val listMessages = mutableListOf<Message>()

    lateinit var popup: PopupMenu

    lateinit var profilePictureView: ImageView
    lateinit var textFullName: TextView
    lateinit var messagesList: ListView
    var messageAdapter: MessagesListAdapter? = null
    var groupMessageAdapter: MessagesListAdapterGroupChat? = null

    lateinit var messagesRef: CollectionReference

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    lateinit var chatId: String
    var otherUserUid: String? = null
    lateinit var otherUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        //Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarChat)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Intent
        chatId = intent.getStringExtra("chatId")!!
        otherUserUid = intent.getStringExtra("otherUserUid")
        val otherUserDisplayName = intent.getStringExtra("otherUserDisplayName")
        val profileImage = intent.getStringExtra("otherUserProfileImage")

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserFb = auth.currentUser

        messagesRef = db.collection("chats").document(chatId).collection("messages")

        profilePictureView = findViewById(R.id.imageProfilePicture)
        textFullName = findViewById(R.id.textFullName)
        messagesList = findViewById(R.id.listViewMessages)
        val moreIcon = findViewById<ImageView>(R.id.iconMore)
        val sendAttachmentButton = findViewById<ImageView>(R.id.buttonSendAttachment)
        val editText = findViewById<EditText>(R.id.textField)
        val sendButton = findViewById<ImageView>(R.id.buttonSend)

        popup = PopupMenu(this, moreIcon)
        val inflater = popup.menuInflater
        popup.setOnMenuItemClickListener(this)
        inflater.inflate(R.menu.menu_chat, popup.menu)

        textFullName.text = otherUserDisplayName

        if(profileImage != null && profileImage.isNotEmpty()){
            Picasso.get()
                .load(profileImage)
                .resize(48, 48)
                .into(profilePictureView, object : Callback {
                    override fun onSuccess() {
                        Log.d("ChatListAdapter-picasso", "success")
                    }
                    override fun onError(e: Exception?) {
                        Log.d("ChatListAdapter-picasso", "error")
                    }
                })
        } else profilePictureView.setImageResource(R.drawable.bg_no_pfp)

        createChat()

        moreIcon.setOnClickListener { popup.show() }

        sendButton.setOnClickListener {
            val message = Message(currentUserFb!!.uid, editText.text.toString())

            //If the field is not empty, send a message
            if (editText.text.isNotBlank()) {
                messagesRef.add(message)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            if(messageAdapter != null)messageAdapter!!.notifyDataSetChanged() else groupMessageAdapter!!.notifyDataSetChanged()
                                messagesList.smoothScrollToPosition(listMessages.size - 1)
                        } else {
                            Log.e(TAG, it.exception.toString())
                        }
                    }
                db.collection("chats").document(chatId).set(hashMapOf("timestamp" to FieldValue.serverTimestamp()), SetOptions.merge())

                //clear the field when sending a message
                editText.setText("")
            }
        }

        editText.setOnClickListener {
            messagesList.smoothScrollToPosition(listMessages.size - 1)
        }
    }

    fun createChat() {
        var currentUser: User?
        db.collection("users")
            .document(currentUserFb!!.uid)
            .get()
            .addOnCompleteListener {
            if (it.isSuccessful) {
                val currentUserAttributes = it.result!!.toObject(User::class.java)!!
                val email = currentUserAttributes.email!!
                val username = currentUserAttributes.username!!
                val displayName = currentUserAttributes.displayName!!
                currentUser = User(currentUserFb!!.uid, email, username, displayName)

                //starts a group chat if the intent tells it to
                if(otherUserUid != null) getMessages(currentUser!!) else getGroupMessages(currentUser!!)

            } else Log.e("ChatActivity", it.exception.toString())
        }
    }

    private fun getGroupMessages(currentUser: User) {
        db.collection("chats")
            .document(chatId)
            .get()
            .addOnCompleteListener {
                val otherUsers = mutableListOf<User>()

                groupMessageAdapter = MessagesListAdapterGroupChat(this, listMessages, currentUser, otherUsers)
                messagesList.adapter = groupMessageAdapter

                val userUids = it.result!!["users"] as List<String>

                userUids.forEachIndexed { index, s ->
                    if(s != auth.currentUser!!.uid){
                        db.collection("users")
                            .document(s)
                            .get()
                            .addOnCompleteListener {
                                val otherUserDoc = it.result!!.toObject(User::class.java)!!
                                otherUsers.add(otherUserDoc)
                                groupMessageAdapter!!.notifyDataSetChanged()
                            }
                    }
                }
                setSnapshotListener()
                if(listMessages.size > 0) listMessages.sortBy { it.timestamp }
            }
    }

    fun getMessages(currentUser: User) {
        db.collection("users")
            .document(otherUserUid!!)
            .get()
            .addOnCompleteListener {
                otherUser = it.result!!.toObject(User::class.java)!!

                messageAdapter = MessagesListAdapter(this, listMessages, currentUser, otherUser)
                messagesList.adapter = messageAdapter

                setSnapshotListener()
                checkIfFriend()
                if(listMessages.size > 0) listMessages.sortBy { it.timestamp }
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
                if(messageAdapter != null ) messageAdapter!!.notifyDataSetChanged() else groupMessageAdapter!!.notifyDataSetChanged()
                messagesList.smoothScrollToPosition(listMessages.size - 1)
//                if(listMessages.size > 0) listMessages.sortBy { it.timestamp }
            }
    }



    fun checkIfFriend() {
        var isFriend: Boolean
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("friends")
            .document(otherUserUid!!)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    isFriend = it.result!!.exists()
                    popup.menu.findItem(R.id.addFriend).isVisible = !isFriend
                }
            }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_chat, menu)
    }

    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        return when (p0?.itemId) {
            R.id.profile -> {
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.addFriend -> {
                db.collection("users")
                    .document(auth.currentUser!!.uid)
                    .collection("friends")
                    .document(otherUserUid!!)
                    .set(otherUser)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            Toast.makeText(this, getString(R.string.s_added_as_friend, otherUser.displayName), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, getString(R.string.could_not_add_s_as_friend, otherUser.displayName), Toast.LENGTH_SHORT).show()
                            Log.e(TAG, it.exception.toString())
                        }
                }
                true
            }
            R.id.block -> {
                Toast.makeText(this, "Block", Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}