package com.example.mobileapp.data.remote.api

import com.example.mobileapp.data.remote.model.inventario.InventarioDTO
import retrofit2.Response
import retrofit2.http.*

interface InventarioApi {

    @POST("api/v1/inventarios")
    suspend fun createInventario(
        @Header("X-Session-Id") sessionId: String,
        @Body inventario: InventarioDTO
    ): Response<InventarioDTO>

    @GET("api/v1/inventarios")
    suspend fun findAll(
        @Header("X-Session-Id") sessionId: String
    ): Response<List<InventarioDTO>>

    @GET("api/v1/inventarios/{id}")
    suspend fun findById(
        @Header("X-Session-Id") sessionId: String,
        @Path("id") id: Long
    ): Response<InventarioDTO>

    @GET("api/v1/inventarios/libro/{libroId}")
    suspend fun findByLibroId(
        @Header("X-Session-Id") sessionId: String,
        @Path("libroId") libroId: Long
    ): Response<InventarioDTO>

    @PUT("api/v1/inventarios/{id}")
    suspend fun updateInventario(
        @Header("X-Session-Id") sessionId: String,
        @Path("id") id: Long,
        @Body inventario: InventarioDTO
    ): Response<InventarioDTO>

    @DELETE("api/v1/inventarios/{id}")
    suspend fun deleteInventario(
        @Header("X-Session-Id") sessionId: String,
        @Path("id") id: Long
    ): Response<Unit>
}