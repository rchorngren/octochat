package com.example.octochat.messaging

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.octochat.R
import com.example.octochat.messaging.util.FriendsListAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FriendsListActivity : AppCompatActivity() {

    val TAG = "FriendsListActivity"
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    lateinit var friendsAdapter: FriendsListAdapter
    val friendsList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val listView = findViewById<ListView>(R.id.listViewFriends)
        val addFriendFab = findViewById<FloatingActionButton>(R.id.fabAddFriend)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        getFriends()

        friendsAdapter = FriendsListAdapter(this, friendsList)
        listView.adapter = friendsAdapter

        addFriendFab.setOnClickListener {
            addFriendDialog()
        }
    }

    private fun addFriendDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val otherUserField = EditText(this)
        otherUserField.hint = "Enter username"

        dialogBuilder.setView(otherUserField)
        dialogBuilder.setTitle(resources.getString(R.string.add_friend))

        dialogBuilder.setPositiveButton(resources.getString(R.string.add)) { dialogInterface, i ->

            val otherUserFieldValue = otherUserField.text.toString()
            if (otherUserFieldValue != "") {
                db.collection("users")
                    .whereEqualTo("username", otherUserFieldValue)
                    .get()
                    .addOnCompleteListener {
                        if (it.result!!.documents.size > 0) {
                            val otherUser = it.result!!.documents[0].toObject(User::class.java)!!

                            db.collection("users")
                                .document(auth.currentUser!!.uid)
                                .collection("friends")
                                .document(otherUser.userId!!)
                                .set(otherUser).addOnCompleteListener {
                                    Toast.makeText(this, "${otherUser.displayName} added as friend", Toast.LENGTH_SHORT).show()
                                    getFriends()
                                }
                        } else {
                            Log.e("ChatListActivity", "No user with username $otherUserFieldValue found; " + it.exception.toString())
                            Toast.makeText(this, "No user with username $otherUserFieldValue found", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }.show()
    }

    private fun getFriends() {
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("friends")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if (friendsList.size > 0) friendsList.clear()
                    if (it.result!!.documents.size > 0) {
                        for (document in it.result!!.documents) {
                            val newDocument = document.toObject(User::class.java)

                            friendsList.add(newDocument!!)
                        }
                        friendsAdapter.notifyDataSetChanged()
                    }
                }
            }
    }

    fun startChat(pos: Int) {

        db.collection("chats")
            .whereArrayContains("users", auth.currentUser!!.uid)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result!!.documents.forEachIndexed { i, document ->
                        val list = document["users"] as List<String>
                        if(list.contains(friendsList[pos].userId)){
                            finish()
                            val intent = Intent(this, ChatActivity::class.java)
                            intent.putExtra("chatId", it.result!!.documents[i].id)
                            intent.putExtra("otherUserDisplayName", friendsList[pos].displayName)
                            intent.putExtra("otherUserUid", friendsList[pos].userId)
                            intent.putExtra("otherUserProfileImage", friendsList[pos].profileImage)
                            startActivity(intent)
                            return@addOnCompleteListener
                        }
                    }
                    val newChatId = db.collection("chats").document()
                    newChatId.set(hashMapOf("users" to listOf(auth.currentUser!!.uid, friendsList[pos].userId),
                        "timestamp" to FieldValue.serverTimestamp()))
                        .addOnCompleteListener {

                        finish()
                        val intent = Intent(this, ChatActivity::class.java)
                        intent.putExtra("chatId", newChatId.id)
                        intent.putExtra("otherUserDisplayName", friendsList[pos].displayName)
                        intent.putExtra("otherUserUid", friendsList[pos].userId)
                        startActivity(intent)
                        Toast.makeText(this, "Chat started with ${friendsList[pos].displayName}", Toast.LENGTH_SHORT).show()
                    }

                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}