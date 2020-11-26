package com.example.octochat.ui.create

/**
 * Data validation state of the create form.
 */
data class CreateFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)