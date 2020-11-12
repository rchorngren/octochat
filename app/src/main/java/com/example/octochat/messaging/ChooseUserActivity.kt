package com.example.octochat.messaging

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.octochat.R

//Endast för test av olika konton, inte final
class ChooseUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_user)

        val button1 = findViewById<Button>(R.id.button1)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)

        button1.setOnClickListener { startActivityWithLogin(
            "test@test.com",
            "12345678",
            "test1@test1.com",
            "kimpi",
            "Kim Hellman") }
        button2.setOnClickListener { startActivityWithLogin(
            "test1@test1.com",
            "12345678",
            "test@test.com",
            "bertabertsson",
            "Berta Bertsson") }
        button3.setOnClickListener { startActivityWithLogin(
            "test2@test2.com",
            "12345678",
            "test@test.com",
            "micke22",
            "Micke Micksson") }



    }

    fun startActivityWithLogin(email: String, password: String, otherUserEmail: String, username: String, displayName: String){
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("email",email)
        intent.putExtra("password", password)
        intent.putExtra("otherUserEmail", otherUserEmail)
        intent.putExtra("username", username)
        intent.putExtra("displayName", displayName)

        startActivity(intent)
    }

}