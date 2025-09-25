package com.example.mobileapp.data.repository

import com.example.mobileapp.data.remote.api.CarritoApi
import com.example.mobileapp.data.remote.model.carrito.CarritoDTO
import retrofit2.Response

class CarritoRepository(private val api: CarritoApi) {

    suspend fun addToCart(sessionId: String, carrito: CarritoDTO): Response<CarritoDTO> {
        return api.addToCart(sessionId, carrito)
    }

    suspend fun getMyCart(sessionId: String): Response<List<CarritoDTO>> {
        return api.getMyCart(sessionId)
    }

    suspend fun updateCartItem(sessionId: String, itemId: Long, carrito: CarritoDTO): Response<CarritoDTO> {
        return api.updateCartItem(sessionId, itemId, carrito)
    }

    suspend fun removeFromCart(sessionId: String, itemId: Long): Response<Unit> {
        return api.removeFromCart(sessionId, itemId)
    }

    suspend fun clearCart(sessionId: String): Response<Unit> {
        return api.clearCart(sessionId)
    }
}