package com.example.mobileapp.data.repository

import com.example.mobileapp.data.remote.api.CreatePreferenceRequest
import com.example.mobileapp.data.remote.api.MercadoPagoApi
import com.example.mobileapp.data.remote.api.MercadoPagoPreferenceResponse
import com.example.mobileapp.data.remote.api.PaymentStatusResponse
import retrofit2.Response

class MercadoPagoRepository(private val api: MercadoPagoApi) {

    suspend fun createPreference(sessionId: String, compraId: Long): Response<MercadoPagoPreferenceResponse> {
        return api.createPreference(sessionId, CreatePreferenceRequest(compraId))
    }

    suspend fun getPaymentStatus(sessionId: String, compraId: Long): Response<PaymentStatusResponse> {
        return api.getPaymentStatus(sessionId, compraId)
    }
}