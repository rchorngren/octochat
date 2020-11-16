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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.util.*

class EditProfile : AppCompatActivity() {
    private val RequestCode = 0
    private var selectedPhotoUri: Uri? = null
    lateinit var imageView: ImageView
    lateinit var changeProfilePic: ImageView
    lateinit var userName: EditText
    lateinit var db: FirebaseFirestore
    lateinit var storageRef: FirebaseStorage

    //lateinit var imageRef: StorageReference
    var profilepic: ImageView? = null
    lateinit var url: String
    lateinit var itemsRef: CollectionReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState
        )
        setContentView(R.layout.activity_edit_profile)
        imageView = findViewById(R.id.editUserName)
        userName = findViewById(R.id.edit_userName)
        changeProfilePic = findViewById(R.id.editProfilePic)
        profilepic = findViewById(R.id.profilePicture)
        val userList = mutableListOf<User>()
        db = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance()
        itemsRef = db.collection("UserDetails")
        /*  imageRef = storageRef.getReference()
            .child("Users Profile Image")

        url = imageRef.downloadUrl.toString()*/

//        val picasso = Picasso.get()
        Picasso.get()
            .load("https://firebasestorage.googleapis.com/v0/b/chatapp-2a770.appspot.com/o/Users%20Profile%20Image?alt=media&token=c33424b4-6e79-47fe-8c65-a75205f1392a")
            .into(profilepic, object : Callback {
                override fun onSuccess() {
                    Log.d("TAG", "success")
                }

                override fun onError(e: Exception?) {
                    Log.d("TAG", "error")
                }
            })
        edit_userName.setEnabled(false)

        imageView.setOnClickListener {
            edit_userName.setEnabled(true)
        }
        editProfilePic.setOnClickListener {
            val i = Intent()
            i.setType("image/*")
            i.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(i, "Choose Picture"), RequestCode)

        }


        saveButton.setOnClickListener {
            val username = userName.text.toString()
            upload()
            updateUserProfile(username)
            edit_userName.setEnabled(false)
            imageView.setVisibility(View.INVISIBLE)
            changeProfilePic.setVisibility(View.INVISIBLE)
            readFirestoreData()
        }

        /*val itemsRef=   db.collection("users")
        itemsRef.addSnapshotListener { snapshot, e ->
            if( snapshot != null ) {
                userList.clear()
                for (document in snapshot.documents) {
                    val newItem = document.toObject(User::class.java)
                    if ( newItem != null)
                        userList.add(newItem)
                    Log.d("!!!", "${newItem}")
                }
            }
            Log.d("!!!", "List1: ${userList.size}")
        }*/
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data!!
            var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            profilePicture.setImageBitmap(bitmap)
        }
    }

    private fun upload() {
        if (selectedPhotoUri != null) {
            val pd = ProgressDialog(this)
            pd.setTitle("Uploading")
            pd.show()
            val fileRef = FirebaseStorage.getInstance().reference.child("Users Profile Image")

            fileRef.putFile(selectedPhotoUri!!)

                .addOnSuccessListener { p0 ->
                    pd.dismiss()
                    Log.d("!!!", "File uploaded")
                    Toast.makeText(applicationContext, "File uploaded", Toast.LENGTH_SHORT).show()
                }

            Log.d("!!!", "${fileRef.downloadUrl}")

        }
    }

    fun updateUserProfile(username: String) {

        val userList = mutableListOf<User>()

        val db = FirebaseFirestore.getInstance()
        val user: MutableMap<String, Any> = HashMap()
        user["userName"] = username
        if (username == null) {
            error("Please enter name")
        }
        itemsRef.add(user)
            .addOnSuccessListener { document ->
                Toast.makeText(this, "record added successfully ", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "record Failed to add ", Toast.LENGTH_SHORT).show()
            }
    }

    fun readFirestoreData() {

        itemsRef.get()
            .addOnCompleteListener {
                val result: StringBuffer = StringBuffer()
                if (it.isSuccessful) {
                    for (document in it.result!!) {
                        result.append(document.data.getValue("userName")).append("")
                    }
                    userName.setText(result)

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

