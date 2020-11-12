package com.example.octochat

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

        val username = findViewById<EditText>(R.id.textNameOrEmail)
        val password = findViewById<EditText>(R.id.textUserPass)
        lateinit var auth : FirebaseAuth
        val db = FirebaseFirestore.getInstance()

        val addNew = ClipData.Item(username.text.toString())

        val user = auth.currentUser
        if( user == null)
            return
//To continue development
        db.collection("users").document().collection("user").add(addNew)
            .addOnCompleteListener { task ->
                Log.d("AddingNew", "Add: ${task.exception}")

            }

    }
}