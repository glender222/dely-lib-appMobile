package com.example.mobileapp.data.remote.model.compra

data class CompraDTO(
    val idCompra: Long? = null,
    val idUsuario: Long,
    val direccionEnvio: String,
    val distrito: String,
    val calle: String,
    val ciudad: String,
    val fechaPago: String? = null,
    val fechaCreacionEmpaquetado: String? = null,
    val fechaEntrega: String? = null,
    val estadoProcesoCompra: String = "PENDIENTE"
)

data class DetalleCompraDTO(
    val idDetalleCompra: Long? = null,
    val idCompra: Long,
    val idLibro: Long,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
)