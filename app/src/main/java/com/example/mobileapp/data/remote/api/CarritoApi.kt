package com.example.mobileapp.data.remote.api

import com.example.mobileapp.data.remote.model.carrito.CarritoDTO
import retrofit2.Response
import retrofit2.http.*

interface CarritoApi {

    @POST("api/v1/carrito")
    suspend fun addToCart(
        @Header("X-Session-Id") sessionId: String,
        @Body carrito: CarritoDTO
    ): Response<CarritoDTO>

    @GET("api/v1/carrito")
    suspend fun getMyCart(
        @Header("X-Session-Id") sessionId: String
    ): Response<List<CarritoDTO>>

    @PUT("api/v1/carrito/{id}")
    suspend fun updateCartItem(
        @Header("X-Session-Id") sessionId: String,
        @Path("id") itemId: Long,
        @Body carrito: CarritoDTO
    ): Response<CarritoDTO>

    @DELETE("api/v1/carrito/{id}")
    suspend fun removeFromCart(
        @Header("X-Session-Id") sessionId: String,
        @Path("id") itemId: Long
    ): Response<Unit>

    @DELETE("api/v1/carrito")
    suspend fun clearCart(
        @Header("X-Session-Id") sessionId: String
    ): Response<Unit>
}