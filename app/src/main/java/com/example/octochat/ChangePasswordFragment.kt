package com.example.octochat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class ChangePasswordFragment : Fragment() {

    lateinit var currentPasswordInput : TextView
    lateinit var newPasswordInput : TextView
    lateinit var confirmNewPassword : TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_password_change, container,false)

        currentPasswordInput = view.findViewById(R.id.currentPasswordInput)
        newPasswordInput = view.findViewById(R.id.newPasswordInput)
        confirmNewPassword = view.findViewById(R.id.newPasswordConfirmInput)

        var saveButton = view.findViewById<Button>(R.id.saveButton)
        var cancelButton = view.findViewById<Button>(R.id.cancelButton)

        saveButton.setOnClickListener {
            //save password and close fragment - display toast IF save is successful
        }

        cancelButton.setOnClickListener {
            (activity as SettingsActivity).removeChangePasswordFragment()
        }
        return view
    }

}