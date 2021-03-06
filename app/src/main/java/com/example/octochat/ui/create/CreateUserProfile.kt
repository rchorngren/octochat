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
import com.example.octochat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.octochat.ui.login.LoginActivity


class CreateUserProfile : AppCompatActivity()  {
    private lateinit var createViewModel: CreateViewModel
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var displayName: EditText
    lateinit var username: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        displayName = findViewById(R.id.textViewCreateDisplayName)
        username = findViewById(R.id.textViewCreateUsername)
        val email = findViewById<EditText>(R.id.textViewCreateEmail)
        val password = findViewById<EditText>(R.id.textViewCreatePassword)
        val register = findViewById<TextView>(R.id.userCreateAccount_button)
        val loading = findViewById<ProgressBar>(R.id.loading)
        val cancel = findViewById<TextView>(R.id.textView_CancelAtCreate_first)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        createViewModel = ViewModelProvider(this, CreateViewModelFactory())
            .get(CreateViewModel::class.java)
        Log.d("StartThe" , "CreateUser")

        createViewModel.createFormState.observe(this@CreateUserProfile, Observer {
            val createState = it ?: return@Observer

            register.isEnabled = createState.isDataValid

            if (createState.usernameError != null) {
                email.error = getString(createState.usernameError)
            }
            if (createState.passwordError != null) {
                password.error = getString(createState.passwordError)
            }
        })

        createViewModel.createResult.observe(this@CreateUserProfile, Observer {
            val createResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (createResult.error != null) {
                showCreateFailed(createResult.error)
            }

            //Updade this 'if' if UI is to login
            if (createResult.success != null) {
                Log.e("CreateUserProfile", "successNotnull")
                updateUiWithUser(
                    createResult.success,
                    email.text.toString(),
                    password.text.toString()
                )
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy create activity once successful
            //finish()
        })

        email.afterTextChanged {
            createViewModel.createDataChanged(
                email.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                createViewModel.createDataChanged(
                    email.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        createViewModel.create(
                            email.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }
        }


        register.setOnClickListener {
            readCreate(email, password)
        }

        cancel.setOnClickListener{
            Log.d("Cancel", "create")
            finish()
        }

        val cancelStrg: String = getString(R.string.sign_up_cancel)
        val content = SpannableString(cancelStrg)
        content.setSpan(UnderlineSpan(), 0, cancelStrg.length, 0)
        cancel.text = content
    }


    private fun readCreate(username: EditText, password: EditText){
        // TODO : initiate successful logged in experience

        if(password.text.toString().isEmpty()){
            createViewModel.createDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }
        else{
            buildNewAccount(username, password)
        }
    }
    private fun buildNewAccount(usernameView: EditText, passwordView: EditText) {
        val email = usernameView.text.toString()
        val password = passwordView.text.toString()
        Log.d("buildNewAccountMessage", "email: $email, password: $password")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("DoneCreating", "Success")

                    val user = auth.currentUser
                    db.collection("users")
                        .document(user!!.uid)
                        .set(User(user.uid, email, username.text.toString(), displayName.text.toString(),""))

                    finish()
                    //To login
//                    val intent = Intent(this, LoginActivity::class.java)
//                    startActivity(intent)
                    //userFactory()



                }
                else {
                    Toast.makeText(
                        applicationContext,
                        "${getString(R.string.registration_signup_failed)} - ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

//Update this function if UI will sign in the user after creating account
    private fun updateUiWithUser(model: CreatedUserView, username: String, password: String) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName

        Log.d("updateUiWithUser", "email: $username, password: $password")
        auth.signInWithEmailAndPassword(username, password).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("LoginActivity", "Successful login")

                // Comment out to avoid sign in after creating user
                /*val user = auth.currentUser
                db.collection("users").document(user!!.uid).set(
                    User(
                        user.uid,
                        username,
                        username,
                        "New user"
                    )
                )*/
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