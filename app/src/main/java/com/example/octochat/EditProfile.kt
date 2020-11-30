package com.example.octochat

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import com.example.octochat.messaging.User
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.userprofile_bottom_sheet.*
import kotlinx.android.synthetic.main.userprofile_bottom_sheet.view.*

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import com.google.firebase.database.DatabaseError.PERMISSION_DENIED
import com.google.firebase.firestore.FieldValue
import io.grpc.Status.PERMISSION_DENIED


class EditProfile : AppCompatActivity() {
    private val RequestCode = 0
    private var selectedPhotoUri: Uri? = null
    lateinit var editName: TextView
    lateinit var editDisplayName : TextView
    lateinit var changeProfilePic: ImageView
    lateinit var displayName: EditText
    lateinit var editUserName: EditText
    lateinit var db: FirebaseFirestore
    lateinit var storageRef: FirebaseStorage
    lateinit var auth: FirebaseAuth
    var profilepic: ImageView? = null
    lateinit var itemsRef: CollectionReference
    lateinit var url1: String
    val PERMISSION_CODE : Int = 1000
    var imageUri: Uri ?= null

    val IMAGE_CAPTURE_CODE : Int = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        
        editName = findViewById(R.id.editUserName)
        editDisplayName = findViewById(R.id.editDisplayName)
        displayName = findViewById(R.id.edit_displayName)
        editUserName =findViewById(R.id.edit_userName)




        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        changeProfilePic = findViewById(R.id.editProfilePic)
        profilepic = findViewById(R.id.profilePicture)

        db = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance()
        itemsRef = db.collection("users")
        auth = FirebaseAuth.getInstance()

        readFirestoreData()

        displayName.setEnabled(false)
        editUserName.setEnabled(false)

        editDisplayName.setOnClickListener {

            displayName.setEnabled(true)
        }
        editName.setOnClickListener {

            editUserName.setEnabled(true)
        }
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.userprofile_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        editProfilePic.setOnClickListener {

        bottomSheetDialog.show()
        }
        view.takePicCamera.setOnClickListener {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED){
                        val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        requestPermissions(permission,PERMISSION_CODE)
                        }
                    else {
                        takePhoto()
                    }

            }
                else {
            takePhoto()

        }
        bottomSheetDialog.dismiss()
        }
        view.uploadFromPhotos.setOnClickListener {
            uploadImageGallery()
        }

        saveButton.setOnClickListener {
            updateDisplayName()
            updateUserName()
            displayName.setEnabled(false)
            editUserName.setEnabled(false)
            changeProfilePic.setVisibility(View.INVISIBLE)

            val intent = Intent(this, UserProfile::class.java)
               startActivity(intent)

        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if(grantResults.size >0 && grantResults [0] ==
                        PackageManager.PERMISSION_GRANTED)
                {
                    takePhoto()

                } else {
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun takePhoto(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE)

    }

    fun uploadImageGallery(){
           val i = Intent()
           i.setType("image/*")
           i.setAction(Intent.ACTION_GET_CONTENT)
           startActivityForResult(Intent.createChooser(i, "Choose Picture"), RequestCode)
           }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {
            selectedPhotoUri = data.data
            var bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            profilepic?.setImageBitmap(bitmap)
            uploadPhotos()

        } else (resultCode == Activity.RESULT_OK)
            profilepic?.setImageURI(imageUri)
            uploadCamera()

    }

    fun uploadCamera(){
        val user = auth.currentUser
    if (imageUri != null) {
        val fileRef =
            FirebaseStorage.getInstance().reference.child(user!!.uid).child("UserProfileImage").child("CameraImage")
        fileRef.putFile(imageUri!!).addOnSuccessListener() { taskSnapshot ->
            fileRef.downloadUrl.addOnCompleteListener() { taskSnapshot ->
                var url = taskSnapshot.result
                url1 = url.toString()
                Log.d("!!!", "$url1")

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
                db.collection("users").document(user!!.uid).set(hashMapOf("profileImage" to url1), SetOptions.merge())

            }
        }. addOnFailureListener{
            Toast.makeText(applicationContext, "Profile pic not uploaded", Toast.LENGTH_SHORT)
                .show()

        }
    }

    }

    private fun uploadPhotos() {
        val user = auth.currentUser
        if (selectedPhotoUri != null) {
            val pd = ProgressDialog(this)
            pd.setTitle("Uploading")
            pd.show()
            val fileRef =
                FirebaseStorage.getInstance().reference.child(user!!.uid).child("UserProfileImage").child("PhotoImages")
            fileRef.putFile(selectedPhotoUri!!).addOnSuccessListener() { taskSnapshot ->
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
                    db.collection("users").document(user!!.uid).set(hashMapOf("profileImage" to url1), SetOptions.merge())

                }
            }. addOnFailureListener{
                         Toast.makeText(applicationContext, "Profile pic not uploaded", Toast.LENGTH_SHORT)
                          .show()
                         pd.dismiss()
                    }
              }
    }

    fun readFirestoreData() {
        val testViewName =findViewById<TextView>(R.id.tv_name)
        val email = findViewById<TextView>(R.id.textEmail)

        val user = auth.currentUser
        user?.let {
            val userEmail = user.email
            db.collection("users").document(user!!.uid).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val currentUser = it.result!!.toObject(User::class.java)!!
                    val display_Name = currentUser.displayName
                    val profileImage = currentUser.profileImage
                    val name = currentUser.username

                        Picasso.get()
                            .load(profileImage)
                            .into(profilepic, object : Callback {
                                override fun onSuccess() {
                                    Log.d("TAG", "success")
                                }

                                override fun onError(e: Exception?) {
                                    Log.d("TAG", "error")
                                }
                            })
                    
                    testViewName.setText(display_Name)
                    displayName.setText(display_Name)
                    editUserName.setText(name)
                }
            }
            email.setText(userEmail)

        }
    }


    fun updateDisplayName(){

        val user = auth.currentUser
        val name = displayName.text.toString().trim()
        if (name.isEmpty())
        {
            displayName.error= "Please enter the name"
            displayName.requestFocus()

        }


            db.collection("users").document(user!!.uid).set(hashMapOf("displayName" to name), SetOptions.merge())
            Log.d("success", "Success")
            Toast.makeText(applicationContext, "display name updated", Toast.LENGTH_SHORT).show()
       }

    fun updateUserName(){

        val user = auth.currentUser
        val name = editUserName.text.toString().trim()
        if (name.isEmpty())
        {
            editUserName.error= "Please enter the name"
            editUserName.requestFocus()

        }
        db.collection("users").document(user!!.uid).set(hashMapOf("username" to name), SetOptions.merge())
        Log.d("success", "Success")
        Toast.makeText(applicationContext, "display name updated", Toast.LENGTH_SHORT).show()





    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()

    }

}




