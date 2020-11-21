package com.example.octochat.ui.create

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import androidx.lifecycle.Observer
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import com.example.octochat.messaging.User
//import com.example.octochat.ui.create.LoggedInUserView
import com.example.octochat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.octochat.ui.login.LoginActivity


class CreateUserProfile : AppCompatActivity()  {
    private lateinit var createViewModel: CreateViewModel
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        //This user_first_name is, different from username a name to bill to, for payments or other
        val user_first_name = findViewById<EditText>(R.id.textUserNameAtCreate_first)

        //Important - this username is the name send to database for registration and login. On create-models username uses this origin
        val username = findViewById<EditText>(R.id.textUser_EmailAtCreate_first)

        val password = findViewById<EditText>(R.id.textUser_PasswordAtCreate_first)
        //val login = findViewById<Button>(R.id.userLogIn_buttonAtFirst)
        val register = findViewById<TextView>(R.id.userCreateAccount_button)
        val loading = findViewById<ProgressBar>(R.id.loading)
        val cancel = findViewById<TextView>(R.id.textView_CancelAtCreate_first)
        val intent = Intent(this, LoginActivity::class.java)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        createViewModel = ViewModelProvider(this, CreateViewModelFactory())
            .get(CreateViewModel::class.java)
        Log.d("StartThe" , "CreateUser")

        createViewModel.createFormState.observe(this@CreateUserProfile, Observer {
            val createState = it ?: return@Observer

            // disable create button unless both username / password is valid
            register.isEnabled = createState.isDataValid

            if (createState.usernameError != null) {
                username.error = getString(createState.usernameError)
            }
            if (createState.passwordError != null) {
                password.error = getString(createState.passwordError)
            }
        })

        createViewModel.createResult.observe(this@CreateUserProfile, Observer {
            val createResult = it ?: return@Observer

            //Log.e("Nowlog" , createResult.toString())

            loading.visibility = View.GONE
            if (createResult.error != null) {
                showCreateFailed(createResult.error)
            }
            if (createResult.success != null) {
                Log.e("CreateUserProfile", "successNotnull")
                updateUiWithUser(
                    createResult.success,
                    username.text.toString(),
                    password.text.toString()
                )
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy create activity once successful
            //finish()
        })

        username.afterTextChanged {
            createViewModel.createDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }


        password.apply {
            afterTextChanged {
                createViewModel.createDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        createViewModel.create(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }
        }

        register.setOnClickListener{
            Log.d("thisIsBeing", "clicked - ")
            loading.visibility = View.VISIBLE
            buildNewAccount(username, password)
            //createViewModel.create(username.text.toString(), password.text.toString())
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


    private fun updateUiWithUser(model: CreatedUserView, username: String, password: String) {
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

    private fun showCreateFailed(@StringRes errorString: Int) {
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