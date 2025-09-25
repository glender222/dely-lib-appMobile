package com.example.mobileapp.presentation.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobileapp.data.repository.GeneroRepository

class GeneroViewModelFactory(
    private val repo: GeneroRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeneroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GeneroViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
