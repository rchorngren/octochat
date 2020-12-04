package com.example.octochat.messaging

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import com.example.octochat.R
import com.example.octochat.SettingsActivity
import com.example.octochat.UserProfile
import com.example.octochat.messaging.util.ChatListAdapter
import com.example.octochat.messaging.util.FriendsListAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
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

        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)

        //fab
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        val startGroupChatFab = findViewById<FloatingActionButton>(R.id.fabMiniStartGroupChat)
        val emailFab = findViewById<FloatingActionButton>(R.id.fabMiniEmail)
        val usernameFab = findViewById<FloatingActionButton>(R.id.fabMiniUsername)
        val thirdFabOption = findViewById<LinearLayout>(R.id.thirdFabOption)
        val secondFabOption = findViewById<LinearLayout>(R.id.secondFabOption)
        val firstFabOption = findViewById<LinearLayout>(R.id.firstFabOption)

        FabAnimation().init(thirdFabOption)
        FabAnimation().init(secondFabOption)
        FabAnimation().init(firstFabOption)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        getActiveChats()
        getSelfUser()

        headerView.setOnClickListener {
            getSelfUser()
        }
        startGroupChatFab.setOnClickListener { startGroupChatDialog() }

        emailFab.setOnClickListener { startChatFromDialog("email") }

        usernameFab.setOnClickListener { startChatFromDialog("username") }

        fab.setOnClickListener {

            fabExpanded = FabAnimation().rotateFab(it, !fabExpanded)

            if(fabExpanded){
                FabAnimation().showIn(thirdFabOption)
                FabAnimation().showIn(secondFabOption)
                FabAnimation().showIn(firstFabOption)
            } else {
                FabAnimation().showOut(thirdFabOption)
                FabAnimation().showOut(secondFabOption)
                FabAnimation().showOut(firstFabOption)
            }
        }

        listViewChats.setOnItemClickListener { adapterView, view, i, l ->
            if(listChats[i].otherUser.userId != null){
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("chatId", listChats[i].chatId)
                intent.putExtra("otherUserDisplayName", listChats[i].otherUser.displayName)
                intent.putExtra("otherUserUid", listChats[i].otherUser.userId)
                intent.putExtra("otherUserProfileImage", listChats[i].otherUser.profileImage)
                startActivity(intent)
            } else {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("chatId", listChats[i].chatId)
                intent.putExtra("otherUserDisplayName", listChats[i].otherUser.displayName)
                intent.putExtra("otherUserProfileImage", listChats[i].otherUser.profileImage)
                startActivity(intent)

            }
        }
    }

    fun hideFabs(){

    }

    fun getSelfUser() {
        db.collection("users").document(auth.currentUser!!.uid).get().addOnCompleteListener {
            if(it.isSuccessful){
                currentUser = it.result!!.toObject(User::class.java)!!
                navDisplayName.text = currentUser.displayName
                val username = "@" + currentUser.username
                navUsername.text = username

                val profileImage = currentUser.profileImage
                Log.e(TAG, profileImage.toString())

                if(profileImage != null && profileImage.isNotEmpty()){
                    Picasso.get()
                        .load(profileImage)
                        .into(navProfilePic, object : Callback {
                            override fun onSuccess() {
                                Log.d(TAG, "success")
                            }
                            override fun onError(e: Exception?) {
                                Log.d(TAG, "error")
                            }
                        })
                } else {
                    navProfilePic.setImageResource(R.drawable.bg_no_pfp)
                }

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

                        //Checks whether or not it's a group chat
                        if(users!!.size > 2){
                            val otherUserUids = mutableListOf<String>()
                            for (user in users){
                                otherUserUids.add(user)
                            }
                            getGroupChat(document, otherUserUids, timestampDate)
                        } else {
                            var otherUserUid: String? = null
                            for (user in users) {
                                otherUserUid = if (user == auth.currentUser!!.uid) continue else user
                            }
                            getChat(document, otherUserUid!!, timestampDate)
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

    fun getChat(document: DocumentSnapshot, otherUserUid: String, timestampDate: Date){
        db.collection("users").document(otherUserUid)
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
                                } else {
                                    listChats.add(Chat(document.id, otherUser, timestamp = timestampDate))
                                }
                            }
                            listChats.sortByDescending { it.timestamp }
                            emptyView.visibility = TextView.GONE
                            chatListAdapter.notifyDataSetChanged()
                        }
                }
            }
    }

    fun getGroupChat(document: DocumentSnapshot, otherUserUids: List<String>, timestampDate: Date) {
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
                    val fakeOtherUser = User(null, null, null, document["name"] as String?, document["image"] as String?)
                    if (it.result!!.documents.size > 0) {//if there are no messages in the group chat
                        val message = it.result!!.documents[0].toObject(Message::class.java)!!

                        if (message.sender == auth.currentUser!!.uid) { //if you sent the message
                            message.text = "You: " + message.text
                            listChats.add(Chat(document.id, fakeOtherUser, message, timestampDate))
                        } else {
                            listChats.add(Chat(document.id, fakeOtherUser, message, timestampDate))
                        }
                    } else {
                        val groupChat = Chat(document.id, fakeOtherUser, timestamp = timestampDate)
                        listChats.add(groupChat)
                    }
                }
                listChats.sortByDescending { it.timestamp }
                emptyView.visibility = TextView.GONE
                chatListAdapter.notifyDataSetChanged()
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

    private fun startGroupChatDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_start_group_chat, null)

        val listViewFriendsDialog = dialogLayout.findViewById<ListView>(R.id.listFriendsDialog)
        val membersToAddTextView = dialogLayout.findViewById<TextView>(R.id.textMembersToAdd)

        //friends list
        val checkedFriends = mutableListOf<Boolean>()
        val friendsList = mutableListOf<User>()
        val selectedFriendsList = mutableListOf<User>()
        val friendsAdapterDialog = FriendsListAdapter(this, friendsList, checkedFriends)
        listViewFriendsDialog.adapter = friendsAdapterDialog


        listViewFriendsDialog.setOnItemClickListener { adapterView, view, i, l ->
            val checkBox = adapterView[i].findViewById<CheckBox>(R.id.checkboxIncludeInGroupChat)

            checkBox.isChecked = !checkBox.isChecked
            checkedFriends[i] = !checkedFriends[i]

            if(checkBox.isChecked){
                selectedFriendsList.add(friendsList[i])
            } else {
                selectedFriendsList.remove(friendsList[i])
            }

            var membersToAddString = ""
            selectedFriendsList.forEachIndexed { index, friend ->

                membersToAddString += if(index == 0) friend.displayName else ", " + friend.displayName
            }
            membersToAddTextView.text = membersToAddString
            friendsAdapterDialog.notifyDataSetChanged()
        }

        dialogBuilder.setTitle("Add members")
        dialogBuilder.setView(dialogLayout)

        db.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("friends")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if (friendsList.size > 0) friendsList.clear()
                    if (it.result!!.documents.size > 0) {
                        for (document in it.result!!.documents) {
                            db.collection("users")
                                .document(document.id)
                                .get()
                                .addOnCompleteListener { friendDoc ->
                                    val newDocument = friendDoc.result!!.toObject(User::class.java) as User
                                    friendsList.add(newDocument)
                                    checkedFriends.add(false)

                                    friendsAdapterDialog.notifyDataSetChanged()
                                }
                        }
                    }
                }
            }

        //When clicking the "start" button
        dialogBuilder.setPositiveButton("Start") { dialogInterface, i ->
            if(selectedFriendsList.size < 2) {
                Toast.makeText(this, getString(R.string.select_more_than_one), Toast.LENGTH_SHORT).show()
            }else{
                val membersToAdd = mutableListOf<String>()
                selectedFriendsList.forEach { membersToAdd.add(it.userId!!) }
                membersToAdd.add(auth.currentUser!!.uid)

                db.collection("chats")
                    .document()
                    .set(hashMapOf("users" to membersToAdd, "timestamp" to FieldValue.serverTimestamp(), "name" to "New Group Chat", "image" to null))
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