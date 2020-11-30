package com.example.octochat.messaging

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.octochat.R
import com.example.octochat.SettingsActivity
import com.example.octochat.UserProfile
import com.example.octochat.messaging.util.ChatListAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chat_list.*
import java.util.*

class ChatListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    val TAG = "ChatListActivity"
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    lateinit var navUsername: TextView
    lateinit var navDisplayName: TextView
    lateinit var navProfilePic: CircleImageView

    lateinit var currentUser: User
    var currentFbUser: FirebaseUser? = null
    val listChats = mutableListOf<Chat>()
    lateinit var chatListAdapter: ChatListAdapter

    lateinit var listViewChats: ListView
    lateinit var progressBar: ProgressBar
    lateinit var emptyView: TextView

    var fabExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)



        //Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarMain)
        setSupportActionBar(toolbar)

        //used for coloring the toolbar and status bar
//        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.themeYellowLight))
//        this.window.statusBarColor = ContextCompat.getColor(this, R.color.themeYellowDark)

        //Navigation drawer functionality
        drawer = findViewById(R.id.drawerLayout)
        toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_banner , R.string.action_sign_in)
        drawer.addDrawerListener(toggle)
        val navigationView: NavigationView = findViewById(R.id.navView)
        navigationView.setNavigationItemSelectedListener(this)

        //Navigation drawer header views
        val headerView = navigationView.getHeaderView(0)
        navDisplayName = headerView.findViewById(R.id.textDisplayNameNav)
        navUsername = headerView.findViewById(R.id.textUsernameNav)
        navProfilePic= headerView.findViewById(R.id.imagePfpNavHeader)
        listViewChats = findViewById(R.id.listView)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        val emailFab = findViewById<FloatingActionButton>(R.id.fabMiniEmail)
        val usernameFab = findViewById<FloatingActionButton>(R.id.fabMiniUsername)
        val firstFabOption = findViewById<LinearLayout>(R.id.firstFabOption)
        val secondFabOption = findViewById<LinearLayout>(R.id.secondFabOption)

        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)

        FabAnimation().init(secondFabOption)
        FabAnimation().init(firstFabOption)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        getActiveChats()
        getSelfUser()

        emailFab.setOnClickListener { startChatFromDialog("email") }

        usernameFab.setOnClickListener { startChatFromDialog("username") }

        fab.setOnClickListener {

            fabExpanded = FabAnimation().rotateFab(it, !fabExpanded)

            if(fabExpanded){
                FabAnimation().showIn(secondFabOption)
                FabAnimation().showIn(firstFabOption)
            } else {
                FabAnimation().showOut(secondFabOption)
                FabAnimation().showOut(firstFabOption)
            }
        }

        listViewChats.setOnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", listChats[i].chatId)
            intent.putExtra("otherUserDisplayName", listChats[i].otherUser.displayName)
            intent.putExtra("otherUserUid", listChats[i].otherUser.userId)
            startActivity(intent)
        }
    }

    fun getSelfUser() {
        db.collection("users").document(auth.currentUser!!.uid).get().addOnCompleteListener {
            if(it.isSuccessful){
                currentUser = it.result!!.toObject(User::class.java)!!
                navDisplayName.text = currentUser.displayName
                val username = "@" + currentUser.username
                navUsername.text = username
                val profileImage = currentUser.profileImage
                Picasso.get()
                    .load(profileImage)
                    .into(navProfilePic, object : Callback {
                        override fun onSuccess() {
                            Log.d("TAG", "success")
                        }

                        override fun onError(e: Exception?) {
                            Log.d("TAG", "error")
                        }
                    })

            } else {
                Log.e(TAG, "Could not find your profile in the database; " + it.exception.toString())
                Toast.makeText(this, "Could not find your profile in the database", Toast.LENGTH_SHORT).show()
                auth.signOut()
                finish()
            }
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
                        if (timestamp != null) timestampDate = timestamp.toDate() else return@forEachIndexed

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
                                                    listChats.sortByDescending { it.timestamp }
                                                } else {
                                                    listChats.add(Chat(document.id, otherUser))
                                                }
                                            }
                                            emptyView.visibility = TextView.GONE
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun startChatFromDialog(mode: String){
        val dialogBuilder = AlertDialog.Builder(this)
        val otherUserField = EditText(this)
        otherUserField.hint = "Enter $mode of recipient"

        dialogBuilder.setView(otherUserField)
        dialogBuilder.setTitle(getString(R.string.start_chat))

        dialogBuilder.setPositiveButton("Start") { dialogInterface, i ->

            val otherUserFieldValue = otherUserField.text.toString()
            if (otherUserFieldValue != "") {
                db.collection("users")
                    .whereEqualTo(mode, otherUserFieldValue)
                    .get()
                    .addOnCompleteListener {
                        if (it.result!!.documents.size > 0) {
                            val otherUser = it.result!!.documents[0].toObject(User::class.java)!!

                            db.collection("chats")
                                .document()
                                .set(hashMapOf("users" to listOf(auth.currentUser!!.uid, otherUser.userId),
                                    "timestamp" to FieldValue.serverTimestamp()))

                            getActiveChats()
                        } else Log.e("ChatListActivity", "No user with $mode $otherUserFieldValue found")
                    }
            }
        }.show()
    }

    override fun onBackPressed() {
        //prevents the user from accidentally going to the empty MainActivity
        this.moveTaskToBack(true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        //handles button presses in the navigation drawer
        drawer.closeDrawer(GravityCompat.START)
        when(item.itemId){
            R.id.profile -> {
                val intent = Intent(this, UserProfile::class.java)
                startActivity(intent)
            }
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.friends -> {
                val intent = Intent(this, FriendsListActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }
}