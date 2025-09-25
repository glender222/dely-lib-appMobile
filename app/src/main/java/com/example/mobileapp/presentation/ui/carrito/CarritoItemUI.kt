package com.example.mobileapp.presentation.ui.carrito

data class CarritoItemUI(
    val idCarrito: Long? = null,
    val idUsuario: Long? = null,
    val idLibro: Long? = null,
    val cantidad: Int = 1,
    val precioUnitario: Double? = null,
    val tituloLibro: String? = null,
    val autorLibro: String? = null,
    val imagenPortada: String? = null
)