package com.example.mobileapp.data.remote.api

import com.example.mobileapp.data.remote.model.LibroDTO
import com.example.mobileapp.data.remote.model.genero.GeneroDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface GeneroApi {

    @GET("api/v1/generos")
    suspend fun findAll(
        @Header("X-Session-Id") sessionId: String
    ): Response<List<GeneroDTO>>

    @POST("api/v1/generos")
    suspend fun createGenero(
        @Header("X-Session-Id") sessionId: String,
        @Body genero: GeneroDTO
    ): Response<GeneroDTO>

    @GET("api/v1/generos/{id}/libros")
    suspend fun findLibrosByGenero(
        @Header("X-Session-Id") sessionId: String,
        @Path("id") generoId: Long
    ): Response<List<LibroDTO>>

    @DELETE("api/v1/generos/{id}")
    suspend fun deleteGenero(
        @Header("X-Session-Id") sessionId: String,
        @Path("id") generoId: Long
    ): Response<Unit>
}
