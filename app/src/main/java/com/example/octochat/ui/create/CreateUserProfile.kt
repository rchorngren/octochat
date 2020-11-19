package com.example.octochat.ui.create

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.StringRes
import com.example.octochat.messaging.User
import com.example.octochat.ui.login.LoggedInUserView
import com.example.octochat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.octochat.ui.login.LoginActivity

class CreateUserProfile : AppCompatActivity()  {

    //private lateinit var viewModel: CreateViewModel
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        val username = findViewById<EditText>(R.id.textNameOrEmail)
        val password = findViewById<EditText>(R.id.textUserPass)
        val login = findViewById<Button>(R.id.userLogIn_button)
        val register = findViewById<TextView>(R.id.userCreateAccount_button)
        val loading = findViewById<ProgressBar>(R.id.loading)
        val cancel = findViewById<TextView>(R.id.textViewCancel)
        val intent = Intent(this, LoginActivity::class.java)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        /*password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                Log.d("nw", "thisLog1")
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }

            signup.setOnClickListener{
                Log.d("ToCreate", "clicked")
                startActivityForResult(intent, 0)
            }
        }*/


        register.setOnClickListener{
            Log.d("thisIsBeing", "clicked - ")
            buildNewAccount(username, password)
            loading.visibility = View.VISIBLE
        }

        cancel.setOnClickListener{
            Log.d("Cancel", "create")
            startActivityForResult(intent,0)
        }

        val cancelStrg: String = getString(R.string.sign_up_cancel)
        val content = SpannableString(cancelStrg)
        content.setSpan(UnderlineSpan(), 0, cancelStrg.length, 0)
        cancel.setText(content)
    }


    private fun buildNewAccount(usernameView: EditText, passwordView: EditText) {
        val email = usernameView.text.toString()
        val password = passwordView.text.toString()
        Log.d("buildNewAccount", "email: $email, password: $password")

        if (email.isEmpty() || password.isEmpty())
            return

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Done", "Success")
                    //To login
                    startActivityForResult(intent,0)
                    //userFactory()
                } else {
                    Log.d("TestAgain", "Unable to create: ${task.exception}")
                }
            }
    }


    private fun updateUiWithUser(model: LoggedInUserView, username: String, password: String) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName

        Log.d("updateUiWithUser", "email: $username, password: $password")
        auth.signInWithEmailAndPassword(username, password).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("LoginActivity", "Successful login")
                val user = auth.currentUser
                db.collection("users").document(user!!.uid).set(
                    User(
                        user.uid,
                        username,
                        username,
                        "New user"
                    )
                )
//                user = auth.currentUser
                finish()
            } else
                Log.e("updateUiWithUser", it.exception.toString())
        }

        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }


    /**
     * Extension function to simplify setting an afterTextChanged action to EditText components.
     */
    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }
}