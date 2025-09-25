package com.example.mobileapp.data.remote.api



import com.example.mobileapp.data.remote.model.pago.MercadoPagoResponse
import retrofit2.Response
import retrofit2.http.*

interface MercadoPagoApi {

    @POST("api/v1/mercadopago/create-preference")
    suspend fun createPreference(
        @Header("X-Session-Id") sessionId: String,
        @Body request: Map<String, Long>
    ): Response<MercadoPagoResponse>

    @GET("api/v1/mercadopago/payment-status/{compraId}")
    suspend fun getPaymentStatus(
        @Header("X-Session-Id") sessionId: String,
        @Path("compraId") compraId: Long
    ): Response<Map<String, Any>>
}