package com.example.mobileapp.data.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.example.mobileapp.data.remote.RetrofitClient
import com.example.mobileapp.data.remote.api.MercadoPagoPreferenceRequest
import com.example.mobileapp.data.remote.api.MercadoPagoResponse

class MercadoPagoService {

    suspend fun createPreferenceAndPay(
        context: Context,
        sessionId: String,
        compraId: Long
    ): Result<String> {
        return try {
            // 1. Crear preferencia en el backend
            val request = MercadoPagoPreferenceRequest(compraId)
            val response = RetrofitClient.mercadoPagoApi.createPreference(sessionId, request)

            if (response.isSuccessful) {
                val preference = response.body()!!

                // 2. Abrir Mercado Pago en el navegador
                openMercadoPago(context, preference.initPoint)

                Result.success("Redirigiendo a Mercado Pago...")
            } else {
                Result.failure(Exception("Error creating preference: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun openMercadoPago(context: Context, initPoint: String) {
        try {
            // Usar Chrome Custom Tabs si está disponible
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()

            customTabsIntent.launchUrl(context, Uri.parse(initPoint))
        } catch (e: Exception) {
            // Fallback: usar navegador por defecto
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(initPoint))
            context.startActivity(intent)
        }
    }

    suspend fun checkPaymentStatus(sessionId: String, compraId: Long): Result<String> {
        return try {
            val response = RetrofitClient.mercadoPagoApi.getPaymentStatus(sessionId, compraId)

            if (response.isSuccessful) {
                val status = response.body()!!
                Result.success(status.estado)
            } else {
                Result.failure(Exception("Error checking payment: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}