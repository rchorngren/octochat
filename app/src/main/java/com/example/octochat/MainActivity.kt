package com.example.octochat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.octochat.ui.login.LoginActivity
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = FirebaseFirestore.getInstance()
        getLogStarted()
    }

    fun getLogStarted(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}