package com.example.octochat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.octochat.messaging.User
import com.example.octochat.ui.login.LoginActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_user_profile.*

class UserProfile : AppCompatActivity() {

    lateinit var userPic : CircleImageView
    lateinit var db: FirebaseFirestore
   lateinit var storageRef: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        userPic = findViewById(R.id.profilePicture)
        db = FirebaseFirestore.getInstance()

        Picasso.get()

            .load("https://firebasestorage.googleapis.com/v0/b/octochat-4d230.appspot.com/o/Flower2.jpg?alt=media&token=c51aef37-a661-42d2-8e92-3952f9f7364e")
            .into(userPic, object : Callback {
                override fun onSuccess() {
                    Log.d("TAG", "success")
                }

                override fun onError(e: Exception?) {
                    Log.d("TAG", "error")
                }
            })

/*        itemsRef.get()
            .addOnCompleteListener {
                val result: StringBuffer = StringBuffer()
                if (it.isSuccessful) {
                    for (document in it.result!!) {
                        result.append(document.data.getValue("displayName")).append("")
        //                result.append(document.data.getValue("email")).append("")
                    }
                    userName.setText(result)
          //          userName1.setText(result)
            //        email.setText(result)

                }


            }*/

        userPic.setOnClickListener{
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)

        }

    }
}