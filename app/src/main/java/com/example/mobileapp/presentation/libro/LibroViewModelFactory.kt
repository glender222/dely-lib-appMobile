package com.example.mobileapp.presentation.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LibroViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibroViewModel::class.java)) {
            return LibroViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
