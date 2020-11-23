package com.example.octochat

import android.content.ClipData
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.octochat.messaging.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class userFactory : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        createNew()

    }

    fun createNew() {
        Log.e("UserFactory", "Login")

        val username = findViewById<EditText>(R.id.textLoginUserNameOrEmail)
        val password = findViewById<EditText>(R.id.textViewCreatePassword)
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