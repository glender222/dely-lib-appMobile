package com.example.mobileapp.data.repository

import com.example.mobileapp.data.remote.api.GeneroApi
import com.example.mobileapp.data.remote.model.LibroDTO
import com.example.mobileapp.data.remote.model.genero.GeneroDTO
import retrofit2.Response

class GeneroRepository(private val api: GeneroApi) {

    // Obtener todos los géneros
    suspend fun findAllGeneros(sessionId: String): Response<List<GeneroDTO>> {
        return api.findAll(sessionId)
    }

    // Obtener libros de un género específico
    suspend fun findLibrosByGenero(sessionId: String, generoId: Long): Response<List<LibroDTO>> {
        return api.findLibrosByGenero(sessionId, generoId)
    }
}
