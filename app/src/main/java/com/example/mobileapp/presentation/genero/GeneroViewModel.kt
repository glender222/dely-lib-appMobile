package com.example.mobileapp.presentation.books

import androidx.lifecycle.*
import com.example.mobileapp.data.remote.model.LibroDTO
import com.example.mobileapp.data.remote.model.genero.GeneroDTO
import com.example.mobileapp.data.repository.GeneroRepository
import kotlinx.coroutines.launch

class GeneroViewModel(private val repo: GeneroRepository) : ViewModel() {

    private val _generos = MutableLiveData<List<GeneroDTO>>()
    val generos: LiveData<List<GeneroDTO>> = _generos

    private val _librosPorGenero = MutableLiveData<Map<Long, List<LibroDTO>>>(emptyMap())
    val librosPorGenero: LiveData<Map<Long, List<LibroDTO>>> = _librosPorGenero

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun cargarGeneros(sessionId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repo.findAllGeneros(sessionId)
                if (response.isSuccessful) {
                    _generos.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error al cargar géneros: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun cargarLibrosPorGenero(sessionId: String, generoId: Long) {
        viewModelScope.launch {
            try {
                val response = repo.findLibrosByGenero(sessionId, generoId)
                if (response.isSuccessful) {
                    val libros = response.body() ?: emptyList()
                    val currentMap = _librosPorGenero.value?.toMutableMap() ?: mutableMapOf()
                    currentMap[generoId] = libros
                    _librosPorGenero.value = currentMap
                } else {
                    _error.value = "Error al cargar libros del género $generoId"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
