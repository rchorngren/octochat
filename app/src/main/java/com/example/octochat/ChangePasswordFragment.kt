package com.example.octochat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordFragment : Fragment() {

    lateinit var auth: FirebaseAuth

    lateinit var newPasswordInput : TextView
    lateinit var confirmNewPassword : TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_password_change, container,false)

        auth = FirebaseAuth.getInstance()

        newPasswordInput = view.findViewById(R.id.newPasswordInput)
        confirmNewPassword = view.findViewById(R.id.newPasswordConfirmInput)

        var saveButton = view.findViewById<Button>(R.id.saveButton)
        var cancelButton = view.findViewById<Button>(R.id.cancelButton)

        saveButton.setOnClickListener {
            matchAndUpdatePassword()
        }

        cancelButton.setOnClickListener {
            (activity as SettingsActivity).removeChangePasswordFragment()
        }
        return view
    }

    private fun matchAndUpdatePassword() {
        val currentUser = auth.currentUser
        var newPsw = newPasswordInput.text.toString()
        var newConfirmPsw = confirmNewPassword.text.toString()

        if(newPsw != newConfirmPsw) {
            Toast.makeText(
                activity,
                "Passwords does not match",
                Toast.LENGTH_SHORT
            ).show();
        } else if (newPsw.length < 9) {
            Toast.makeText(
                activity,
                "Passwords length is too short - a password length of 9 or more is required",
                Toast.LENGTH_SHORT
            ).show();
        } else {
            currentUser!!.updatePassword(newPsw).addOnCompleteListener{ task ->
                if(task.isSuccessful) {
                    Toast.makeText(
                        activity,
                        "Password updated",
                        Toast.LENGTH_SHORT
                    ).show();
                    (activity as SettingsActivity).removeChangePasswordFragment()
                } else {
                    Toast.makeText(
                        activity,
                        "Something went wrong - please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}