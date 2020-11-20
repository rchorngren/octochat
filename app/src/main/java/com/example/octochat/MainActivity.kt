package com.example.octochat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.example.octochat.messaging.ChatListActivity
import com.example.octochat.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

import androidx.constraintlayout.widget.ConstraintLayout

import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {


    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

//        auth.signOut()

        if(auth.currentUser != null)
            startChatList()
        else
            getLogStarted()
    }

    fun getLogStarted(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent,0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            startChatList()
        }
    }

    fun startChatList(){
        Log.e("Main",auth.currentUser!!.uid)
        val intent = Intent(this, ChatListActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}