package com.example.mobileapp.data.remote.api

import com.example.mobileapp.data.remote.model.genero.GeneroLibroDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GeneroLibroApi {

    // Crea la relación libro ↔ género
    @POST("api/v1/genero-libros")
    suspend fun create(
        @Header("X-Session-Id") sessionId: String,
        @Body body: GeneroLibroDTO
    ): Response<GeneroLibroDTO>

    // Lista todas las relaciones
    @GET("api/v1/genero-libros")
    suspend fun findAll(
        @Header("X-Session-Id") sessionId: String
    ): Response<List<GeneroLibroDTO>>

    // Lista los géneros (relaciones) de un libro específico
    @GET("api/v1/genero-libros/libro/{libroId}")
    suspend fun findGenerosByLibroId(
        @Header("X-Session-Id") sessionId: String,
        @Path("libroId") libroId: Long
    ): Response<List<GeneroLibroDTO>>

    // Elimina una relación por su ID
    @DELETE("api/v1/genero-libros/{id}")
    suspend fun deleteById(
        @Header("X-Session-Id") sessionId: String,
        @Path("id") relacionId: Long
    ): Response<Unit>
}
