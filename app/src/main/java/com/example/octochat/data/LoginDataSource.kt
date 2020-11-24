package com.example.octochat.data

import android.util.Log
import com.example.octochat.data.model.LoggedInUser
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(username: String, password: String): Result<LoggedInUser> {
        try {
            // TODO: handle loggedInUser authentication
            val loggingUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "OctoChat Incognito")
            Log.d("loggingUser here" , loggingUser.toString())
            return Result.Success(loggingUser)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}