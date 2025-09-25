package com.example.mobileapp.presentation.books

import androidx.lifecycle.*
import com.example.mobileapp.data.remote.RetrofitClient
import com.example.mobileapp.data.remote.model.LibroDTO
import kotlinx.coroutines.launch

class LibroViewModel : ViewModel() {

    private val _libros = MutableLiveData<List<LibroDTO>>()
    val libros: LiveData<List<LibroDTO>> = _libros

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun cargarLibros(sessionId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = RetrofitClient.libroApi.findAll(sessionId)
                if (response.isSuccessful) {
                    _libros.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
