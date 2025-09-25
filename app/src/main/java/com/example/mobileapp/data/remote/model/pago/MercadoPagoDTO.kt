package com.example.mobileapp.data.remote.model.pago

data class MercadoPagoDTO(
    val preferenceId: String,
    val initPoint: String, // URL para redirección web
    val totalAmount: Double,
    val status: String? = null,
    val externalReference: String? = null // ID de tu compra
)

// Modelo para crear compra con pago
data class CompraConPagoRequest(
    val direccionEnvio: String,
    val distrito: String,
    val calle: String,
    val ciudad: String,
    val libros: List<LibroCompraItem>
)

data class LibroCompraItem(
    val idLibro: Long,
    val cantidad: Int,
    val precioUnitario: Double
)