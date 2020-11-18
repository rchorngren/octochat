package com.example.octochat.ui.login

import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*

import com.example.octochat.R
import com.example.octochat.UserBuild
import com.example.octochat.messaging.User
import com.example.octochat.userFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user_profile.*

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.textNameOrEmail)
        val password = findViewById<EditText>(R.id.textUserPass)
        val login = findViewById<Button>(R.id.userLogIn_button)
        val signup = findViewById<TextView>(R.id.textViewSignUp)
        val loading = findViewById<ProgressBar>(R.id.loading)

        auth = FirebaseAuth.getInstance()
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                Log.e("LoginActivity","successNotnull")
                updateUiWithUser(loginResult.success, username.text.toString(), password.text.toString())
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            //finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
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
                Log.d("nw" ,  "thisLog1")
                loading.visibility = View.VISIBLE


                loginViewModel.login(username.text.toString(), password.text.toString())
            }

            signup.setOnClickListener{
                Log.d("thisIsBeing" ,  "clicked - ")
                buildNewAccount(username , password)
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }
        }
    }
        signup.setOnClickListener{
            Log.d("thisIsBeing" ,  "clicked - ")
            buildNewAccount(username , password)
            loading.visibility = View.VISIBLE
            loginViewModel.login(username.text.toString(), password.text.toString())
        }
        signup.setOnClickListener{
            Log.d("thisIsBeing" ,  "clicked - ")
            buildNewAccount(username , password)
            loading.visibility = View.VISIBLE
            loginViewModel.login(username.text.toString(), password.text.toString())
        }

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
                    userFactory()
                } else {
                    Log.d("TestAgain", "Unable to create: ${task.exception}")
                }
            }
    }


    private fun updateUiWithUser(model: LoggedInUserView, username: String, password: String) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName

        Log.d("updateUiWithUser", "email: $username, password: $password")

                Log.d("Does not exist" , auth.toString())
        auth.signInWithEmailAndPassword(username, password).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("LoginActivity", "Successful login")
                val user = auth.currentUser

                db.collection("users").document(user!!.uid).set(UserBuild(user.uid, username,username, "New user"))
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