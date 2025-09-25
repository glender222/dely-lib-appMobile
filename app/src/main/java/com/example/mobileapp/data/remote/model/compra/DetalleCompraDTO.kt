package com.example.mobileapp.data.remote.model.compra

data class DetalleCompraDTO(
    val idDetalleCompra: Long? = null,
    val idCompra: Long,
    val idLibro: Long,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
)