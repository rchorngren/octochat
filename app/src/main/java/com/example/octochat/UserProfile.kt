package com.example.octochat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.octochat.messaging.User
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
    lateinit var name: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)


        name = findViewById(R.id.displayUser)
        userPic = findViewById(R.id.profilePicture)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        readFirestoreData()
        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
            finish()

        }

    }

    fun readFirestoreData() {
        val email = findViewById<TextView>(R.id.email)
        val displayUser = findViewById<TextView>(R.id.displayUser)
        val displayUserName = findViewById<TextView>(R.id.displayUserName)
        val userName = findViewById<TextView>(R.id.userName)
        val user = auth.currentUser
        user?.let {
            val userEmail = user.email
            db.collection("users").document(user!!.uid).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val currentUser = it.result!!.toObject(User::class.java)!!
                    val displayName = currentUser.displayName
                    val profileImage = currentUser.profileImage
                    val name = currentUser.username
                    Picasso.get()
                        .load(profileImage)
                        .into(userPic, object : Callback {
                            override fun onSuccess() {
                                Log.d("TAG", "success")
                            }

                            override fun onError(e: Exception?) {
                                Log.d("TAG", "error")
                            }
                        })
                    displayUserName.setText(displayName)
                    displayUser.setText(displayName)
                    userName.setText(name)
                }
            }
            email.setText(userEmail)

        }
    }


}
