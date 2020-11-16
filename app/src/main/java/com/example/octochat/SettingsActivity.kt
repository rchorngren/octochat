package com.example.octochat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        Picasso.get()
            .load("https://firebasestorage.googleapis.com/v0/b/chatapp-2a770.appspot.com/o/Users%20Profile%20Image?alt=media&token=c33424b4-6e79-47fe-8c65-a75205f1392a")
            .into(settingsProfilePic, object : Callback {
                override fun onSuccess() {
                    Log.d("TAG", "success")
                }

                override fun onError(e: Exception?) {
                    Log.d("TAG", "error")
                }
            })


        }

}