package com.example.mobileapp.data.remote.model.carrito

data class CarritoDTO(
    val idCarrito: Long? = null,
    val idUsuario: Long,
    val idLibro: Long,
    val cantidad: Int,
    val precioUnitario: Double? = null
)