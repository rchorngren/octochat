package com.example.octochat.ui.create


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.octochat.data.CreateRepository
import com.example.octochat.data.ResultCreate

import com.example.octochat.R


class CreateViewModel(private val createRepository: CreateRepository) : ViewModel() {

    private val _createForm = MutableLiveData<CreateFormState>()
    val createFormState: LiveData<CreateFormState> = _createForm

    private val _createResult = MutableLiveData<CreateResult>()
    val createResult: LiveData<CreateResult> = _createResult


    fun create(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = createRepository.create(username, password)

        Log.d("ShowCreateRes" , result.toString())

        if (result is ResultCreate.Success ) {
            _createResult.value =
                CreateResult(success = CreatedUserView(displayName = result.data.displayName))
            Log.e("ShowRes" ,  _createResult.value.toString())
        } else {
            _createResult.value = CreateResult(error = R.string.create_failed)
        }
    }

    fun createDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _createForm.value = CreateFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _createForm.value = CreateFormState(passwordError = R.string.invalid_password)
        } else {
            _createForm.value = CreateFormState(isDataValid = true)
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