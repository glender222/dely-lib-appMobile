package com.example.mobileapp.data.repository

import com.example.mobileapp.data.remote.api.CompraApi
import com.example.mobileapp.data.remote.api.MercadoPagoApi

import com.example.mobileapp.data.remote.model.pago.CompraConPagoRequest
import com.example.mobileapp.data.remote.model.pago.MercadoPagoDTO
import retrofit2.Response

class PagoRepository(
    private val mercadoPagoApi: MercadoPagoApi,
    private val compraApi: CompraApi
) {

    suspend fun createCompraWithPayment(
        sessionId: String,
        compra: CompraConPagoRequest
    ): Response<Map<String, Any>> {
        return compraApi.createCompraWithPayment(sessionId, compra)
    }

    suspend fun createPreference(
        sessionId: String,
        compraId: Long
    ): Response<MercadoPagoDTO> {
        return mercadoPagoApi.createPreference(sessionId, mapOf("compraId" to compraId))
    }

    suspend fun getPaymentStatus(
        sessionId: String,
        compraId: Long
    ): Response<Map<String, Any>> {
        return mercadoPagoApi.getPaymentStatus(sessionId, compraId)
    }
}