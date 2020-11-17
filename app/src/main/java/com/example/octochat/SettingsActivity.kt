package com.example.octochat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout

class SettingsActivity : AppCompatActivity() {

    //private lateinit var navigateBackButton : ImageView
    private lateinit var changePasswordButton : ConstraintLayout
    private lateinit var logoutConstraint : ConstraintLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //navigateBackButton = findViewById(R.id.navigateBackButton)
        changePasswordButton = findViewById(R.id.changePasswordConstraint)
        logoutConstraint = findViewById(R.id.logoutConstraint)

        /*
        navigateBackButton.setOnClickListener {
            finish()
        }

         */

        changePasswordButton.setOnClickListener{
            //Toggle changePassword fragment
            displayChangePasswordFragment()
        }

        logoutConstraint.setOnClickListener{
            //Logout current user
            Log.d("!!!", "Click!")
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
}