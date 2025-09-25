package com.example.mobileapp.data.remote

import com.example.mobileapp.data.remote.api.AuthApi
import com.example.mobileapp.data.remote.api.CarritoApi
import com.example.mobileapp.data.remote.api.CompraApi
import com.example.mobileapp.data.remote.api.GeneroApi
import com.example.mobileapp.data.remote.api.GeneroLibroApi
import com.example.mobileapp.data.remote.api.InventarioApi
import com.example.mobileapp.data.remote.api.LibroApi
import com.example.mobileapp.data.remote.api.MercadoPagoApi
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:9090/"

    private val logging: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val httpClient: OkHttpClient by lazy {
        val sessionHeaderInterceptor = Interceptor { chain ->
            val original = chain.request()
            val hasHeader = original.headers.names().any { it.equals("X-Session-Id", ignoreCase = true) }
            val sessionId = SessionStore.sessionId
            val request = if (!hasHeader && !sessionId.isNullOrBlank()) {
                original.newBuilder()
                    .addHeader("X-Session-Id", sessionId)
                    .build()
            } else original
            chain.proceed(request)
        }

        OkHttpClient.Builder()
            .addInterceptor(sessionHeaderInterceptor)
            .addInterceptor(logging)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val gson by lazy {
        GsonBuilder()
            .setLenient() // Permite JSON malformado
            .serializeNulls()
            .create()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val libroApi: LibroApi by lazy { retrofit.create(LibroApi::class.java) }
    val generoApi: GeneroApi by lazy { retrofit.create(GeneroApi::class.java) }
    val generoLibroApi: GeneroLibroApi by lazy { retrofit.create(GeneroLibroApi::class.java) }
    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }

    val carritoApi: CarritoApi by lazy { retrofit.create(CarritoApi::class.java) }

    val inventarioApi: InventarioApi by lazy { retrofit.create(InventarioApi::class.java) }

    val compraApi: CompraApi by lazy { retrofit.create(CompraApi::class.java) }
    val mercadoPagoApi: MercadoPagoApi by lazy { retrofit.create(MercadoPagoApi::class.java) }
}