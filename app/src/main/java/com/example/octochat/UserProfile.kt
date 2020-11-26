package com.example.octochat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_user_profile.*


class UserProfile : AppCompatActivity() {

    lateinit var userPic: CircleImageView
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var itemsRef: CollectionReference
    lateinit var imageRef: CollectionReference
    lateinit var name: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        name = findViewById(R.id.displayUser)
        userPic = findViewById(R.id.profilePicture)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        getImageUrl()
        readFirestoreData()
        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
            finish()

        }

    }


    fun readFirestoreData() {
        val user = auth.currentUser
        //reference = FirebaseDatabase.getInstance().reference.child("users").child(user!!.uid)
        val displayUserName = findViewById<TextView>(R.id.displayUserName)
        val displayUser= findViewById<TextView>(R.id.displayUser)
        val email = findViewById<TextView>(R.id.email)
        user?.let {
            val displayName = user.displayName
            val userEmail = user.email
            itemsRef = db.collection("users")
            itemsRef.get()
                .addOnCompleteListener {

                    val result: StringBuffer = StringBuffer()
                    if (it.isSuccessful) {
                        for (document in it.result!!) {
                            result.append(document.data.getValue("email")).append(" ")
                            result.append(document.data.getValue("displayName")).append(" ")
                        }
                        email.setText(userEmail)
                        displayUserName.setText(displayName)
                        displayUser.setText(displayName)
                    }
                }
        }


    }

    fun getImageUrl() {
        val user = auth.currentUser
        user?.let {
            imageRef = db.collection("users")
            imageRef.get()
                .addOnCompleteListener {
                    val result: StringBuffer = StringBuffer()
                    if (it.isSuccessful) {
                        for (document in it.result!!) {
                            result.append(document.data.getValue("profileImage")).append(" ")
                        }
                        val imageUrl = result.toString()
                        Log.d("!!!", "$imageUrl")
                        Picasso.get()
                            .load(imageUrl)
                            .into(userPic, object : Callback {
                                override fun onSuccess() {
                                    Log.d("TAG", "success")
                                }

                                override fun onError(e: Exception?) {
                                    Log.d("TAG", "error")
                                }
                            })


                    }
                }
        }

    }
}