package com.example.mobileapp.presentation.ui.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobileapp.data.repository.InventarioRepository

class InventarioViewModelFactory(private val repository: InventarioRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventarioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}