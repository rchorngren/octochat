/*
package com.example.octochat.ui.create

import androidx.lifecycle.ViewModel
import com.example.octochat.ui.login.LoggedInUserView
import com.example.octochat.ui.login.LoginFormState
import com.example.octochat.ui.login.LoginResult
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.util.Patterns
import com.example.octochat.data.LoginRepository
import com.example.octochat.data.Result


class CreateViewModel (private val loginRepository: LoginRepository) : ViewModel() {
    // TODO: Implement the ViewModel

    private val _createForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _createResult = MutableLiveData<LoginResult>()
    val createResult: LiveData<LoginResult> = _createResult

    fun create(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = createRepository.create(username, password)

        Log.d("ShowCreateRes" , result.toString())

        if (result is Result.Success) {
            _createResult.value =



                CreateResult(success = LoggedInUserView(displayName = result.data.displayName))




            Log.e("ShowRes" ,  _loginResult.value.toString())
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@') && username.contains('.')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 7
    }
}
*/
