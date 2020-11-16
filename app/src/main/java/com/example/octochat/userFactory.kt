package com.example.octochat

import com.example.octochat.messaging.User
import android.content.ClipData
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class userFactory : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNew()
    }

    fun createNew() {
        Log.e("UserFactory", "Login")

        val username = findViewById<EditText>(R.id.textNameOrEmail)
        val password = findViewById<EditText>(R.id.textUserPass)
        lateinit var auth : FirebaseAuth
        val db = FirebaseFirestore.getInstance()

        val addNew = ClipData.Item(username.text.toString())

        val user = auth.currentUser
        if( user == null) return

        //To continue development
//        db.collection("users").document().collection("user").add(addNew)
//            .addOnCompleteListener { task ->
//                Log.d("AddingNew", "Add: ${task.exception}")
//
//            }

        db.collection("users").document(user.uid).set(User(user.uid, username.text.toString(),username.text.toString(), "New user"))

    }
}