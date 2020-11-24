package com.example.octochat.data

import android.util.Log
import com.example.octochat.data.model.CreatedUser
import java.io.IOException

/**
 * Class that handles authentication w/ create credentials and retrieves user information.
 */
class CreateDataSource {

    fun create(username: String, password: String): ResultCreate<CreatedUser> {
        try {
            // TODO: handle loggedInUser authentication
            val newUser = CreatedUser(java.util.UUID.randomUUID().toString(), "OctoChat Incognito")
            Log.d("newUser here" , newUser.toString())
            return ResultCreate.Success(newUser)
        } catch (e: Throwable) {
            return ResultCreate.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}