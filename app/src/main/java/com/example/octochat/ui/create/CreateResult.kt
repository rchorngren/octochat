package com.example.octochat.ui.create

/**
 * Authentication result : success (user details) or error message.
 */
data class CreateResult(
    val success: CreatedUserView? = null,
    val error: Int? = null
)