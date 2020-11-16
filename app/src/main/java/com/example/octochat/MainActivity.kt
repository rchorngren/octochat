package com.example.octochat


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_edit_profile.profilePicture
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var userName : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        userName = findViewById(R.id.editText_userName)
        //val userName = editText_userName.text

        //val phNum = editText_phoneNumber.text
        //val email = editText_email.text
        userName.setEnabled(false)

        Picasso.get()
            .load("https://firebasestorage.googleapis.com/v0/b/chatapp-2a770.appspot.com/o/Users%20Profile%20Image?alt=media&token=c33424b4-6e79-47fe-8c65-a75205f1392a")
            .into(profilePicture, object : Callback {
                override fun onSuccess() {
                    Log.d("TAG", "success")
                }

                override fun onError(e: Exception?) {
                    Log.d("TAG", "error")
                }
            })


    }




    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_editprofile -> {
                val intent = Intent(this, EditProfile::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}