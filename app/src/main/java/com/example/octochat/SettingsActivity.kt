package com.example.octochat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SettingsActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    private lateinit var changePasswordButton : ConstraintLayout
    private lateinit var logoutConstraint : ConstraintLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()

        changePasswordButton = findViewById(R.id.changePasswordConstraint)
        logoutConstraint = findViewById(R.id.logoutConstraint)

        changePasswordButton.setOnClickListener{
            displayChangePasswordFragment()
        }

        logoutConstraint.setOnClickListener{
            logoutUser()
        }

    }

    fun displayChangePasswordFragment() {
        val changePasswordFragment = ChangePasswordFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.changePasswordContainer, changePasswordFragment, "changePasswordFragment")
        transaction.commit()
    }

    fun removeChangePasswordFragment() {
        val changePasswordFragment = supportFragmentManager.findFragmentByTag("changePasswordFragment")
        if(changePasswordFragment != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.remove(changePasswordFragment)
            transaction.commit()
        }
    }

    fun logoutUser() {
        val intent = Intent(this, MainActivity::class.java)
        auth.signOut()
        startActivity(intent)
    }

}