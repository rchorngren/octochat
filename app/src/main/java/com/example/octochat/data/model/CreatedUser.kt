package com.example.octochat.data.model

/**
 * Data class that captures user information for logged in users retrieved from CreateRepository
 */
data class CreatedUser(
    val userId: String,
    val displayName: String
)