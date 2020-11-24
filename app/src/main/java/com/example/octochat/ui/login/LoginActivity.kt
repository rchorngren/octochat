package com.example.octochat.ui.login

import android.app.Activity
import android.content.Intent
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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.octochat.R
import com.example.octochat.ui.create.CreateUserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var itemsRef: CollectionReference
    lateinit var storageRef: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.textLoginEmail)
        val password = findViewById<EditText>(R.id.textLoginPassword)
        val login = findViewById<Button>(R.id.userLogIn_buttonAtFirst)
        val signup = findViewById<TextView>(R.id.textView_To_SignUp_Activity)
        val loading = findViewById<ProgressBar>(R.id.loading)

        val intent = Intent(this, CreateUserProfile::class.java)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance()
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
                Log.e("LoginActivity", "successNotnull")
                updateUiWithUser(
                    loginResult.success,
                    username.text.toString(),
                    password.text.toString()
                )
            }
            setResult(Activity.RESULT_OK)
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
                Log.d("nw", "thisLog1")
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }

            signup.setOnClickListener{
                Log.d("ToCreate", "clicked")
                startActivityForResult(intent, 0)
            }
        }

        val signUpStrg: String = getString(R.string.sign_up)
        val content = SpannableString(signUpStrg)
        content.setSpan(UnderlineSpan(), 0, signUpStrg.length, 0)
        signup.text = content
    }

    private fun updateUiWithUser(model: LoggedInUserView, username: String, password: String) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName

        Log.d("updateUiWithUser", "email: $username, password: $password")
        auth.signInWithEmailAndPassword(username, password).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("LoginActivity", "Successful login")
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


