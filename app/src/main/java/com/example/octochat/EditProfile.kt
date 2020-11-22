package com.example.octochat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.octochat.messaging.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*


class EditProfile : AppCompatActivity() {
    private val RequestCode = 0
    private var selectedPhotoUri: Uri? = null
    lateinit var imageView: ImageView
    lateinit var changeProfilePic: ImageView
    lateinit var userName: EditText
    lateinit var db: FirebaseFirestore
    lateinit var storageRef: FirebaseStorage
    lateinit var auth: FirebaseAuth
    var profilepic: ImageView? = null
    lateinit var itemsRef: CollectionReference
    lateinit var imageRef: CollectionReference
    lateinit var url1: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState
        )
        setContentView(R.layout.activity_edit_profile)
        imageView = findViewById(R.id.editUserName)
        userName = findViewById(R.id.edit_userName)
        changeProfilePic = findViewById(R.id.editProfilePic)
        profilepic = findViewById(R.id.profilePicture)
        val userList = mutableListOf<User>()
        db = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance()
        itemsRef = db.collection("users")
        auth = FirebaseAuth.getInstance()
        /*  imageRef = storageRef.getReference()
            .child("Users Profile Image")

        url = imageRef.downloadUrl.toString()*/

//        val picasso = Picasso.get()


        edit_userName.setEnabled(false)

        imageView.setOnClickListener {
            edit_userName.setEnabled(true)
        }
        getImageUrl()

        editProfilePic.setOnClickListener {
            val i = Intent()
            i.setType("image/*")
            i.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(i, "Choose Picture"), RequestCode)

        }


        saveButton.setOnClickListener {
            val username = userName.text.toString()
            upload()
            edit_userName.setEnabled(false)
            imageView.setVisibility(View.INVISIBLE)
            changeProfilePic.setVisibility(View.INVISIBLE)

        }

        readFirestore()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {
            selectedPhotoUri = data.data
            var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            profilepic?.setImageBitmap(bitmap)
        }
    }

    private fun upload() {
        if (selectedPhotoUri != null) {
            val pd = ProgressDialog(this)
            pd.setTitle("Uploading")
            pd.show()
            val fileRef =
                FirebaseStorage.getInstance().reference.child("UserProfileImage").child("images")
            fileRef.putFile(selectedPhotoUri!!).addOnSuccessListener() { taskSnapshot ->
                // success
                fileRef.downloadUrl.addOnCompleteListener() { taskSnapshot ->

                    var url = taskSnapshot.result
                     url1 = url.toString()
                    Log.d("!!!", "$url1")
                    pd.dismiss()
                    Picasso.get()
                        .load(url1)
                        .into(profilepic, object : Callback {
                            override fun onSuccess() {
                                Log.d("TAG", "success")
                            }

                            override fun onError(e: Exception?) {
                                Log.d("TAG", "error")
                            }

                        })
                    val user = auth.currentUser
                    db.collection("image").document("vb2jtIxI37Mwy9m0ezLnBqY4QCo1").set(Image(url1))

                }
            }. addOnFailureListener{
                         Toast.makeText(applicationContext, "Profile pic not uploaded", Toast.LENGTH_SHORT)
                          .show()
                         pd.dismiss()
                    }
              }
    }

    fun getImageUrl() {
        val user = auth.currentUser
        user?.let {
            imageRef = db.collection("image")
            imageRef.get()
                .addOnCompleteListener {
                    val result: StringBuffer = StringBuffer()
                    if (it.isSuccessful) {
                        for (document in it.result!!) {
                            result.append(document.data.getValue("image")).append(" ")
                        }
                        val imageUrl = result.toString()
                        Log.d("!!!", "$imageUrl")
                        Picasso.get()
                            .load(imageUrl)
                            .into(profilepic, object : Callback {
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

    fun readFirestore() {
        val user = auth.currentUser
        //reference = FirebaseDatabase.getInstance().reference.child("users").child(user!!.uid)
        val email1 = findViewById<TextView>(R.id.textEmail)
        user?.let {
            val name = user.displayName
            val userEmail = user.email
            itemsRef = db.collection("users")
            itemsRef.get()
                .addOnCompleteListener {
                    val result: StringBuffer = StringBuffer()
                    if (it.isSuccessful) {
                        for (document in it.result!!) {
                            result.append(document.data.getValue("email")).append(" ")
                        }
                        email1.setText(userEmail)
                    }
                }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                true
            }
            R.id.menu_editprofile -> {
                val intent = Intent(this, UserProfile::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}