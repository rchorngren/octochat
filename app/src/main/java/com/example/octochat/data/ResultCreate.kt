package com.example.octochat.data

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class ResultCreate<out T : Any> {

    data class Success<out T : Any>(val data: T) : ResultCreate<T>()
    data class Error(val exception: Exception) : ResultCreate<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}