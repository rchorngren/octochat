package com.example.octochat.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.octochat.data.CreateDataSource
import com.example.octochat.data.CreateRepository

/**
 * ViewModel provider factory to instantiate CreateViewModel.
 * Required given CreateViewModel has a non-empty constructor
 */
class CreateViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateViewModel::class.java)) {
            return CreateViewModel(
                createRepository = CreateRepository(
                    dataSource = CreateDataSource()
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}