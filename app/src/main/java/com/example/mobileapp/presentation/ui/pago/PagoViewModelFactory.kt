package com.example.mobileapp.presentation.ui.pago

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobileapp.data.repository.PagoRepository

class PagoViewModelFactory(private val repository: PagoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PagoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PagoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}