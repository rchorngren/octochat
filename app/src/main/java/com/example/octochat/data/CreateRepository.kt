package com.example.octochat.data

import android.util.Log
import com.example.octochat.data.model.CreatedUser

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class  CreateRepository(val dataSource: CreateDataSource) {

    // in-memory cache of the loggedInUser object
    var user: CreatedUser? = null
        private set

    val isCreated: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    fun create(username: String, password: String): ResultCreate<CreatedUser> {
        // handle create
        val result = dataSource.create(username, password)

        if (result is ResultCreate.Success) {
            setCreatedUser(result.data)
        }

        return result
    }

    private fun setCreatedUser(createdUser: CreatedUser) {
        this.user = createdUser
        Log.e("LogUser" , this.user.toString())
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}