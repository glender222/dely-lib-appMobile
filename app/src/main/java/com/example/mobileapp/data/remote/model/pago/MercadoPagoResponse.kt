package com.example.mobileapp.data.remote.model.pago

data class MercadoPagoResponse(
    val preferenceId: String,
    val initPoint: String, // URL para abrir en navegador
    val totalAmount: Double,
    val status: String? = null,
    val externalReference: String? = null
)