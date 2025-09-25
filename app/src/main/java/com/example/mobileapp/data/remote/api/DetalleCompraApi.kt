package com.example.mobileapp.data.remote.api

import com.example.mobileapp.data.remote.model.compra.DetalleCompraDTO
import retrofit2.Response
import retrofit2.http.*

interface DetalleCompraApi {

    @POST("api/v1/detalle-compras")
    suspend fun crearDetalleCompra(
        @Header("X-Session-Id") sessionId: String,
        @Body detalle: DetalleCompraDTO
    ): Response<DetalleCompraDTO>

    @GET("api/v1/detalle-compras/compra/{compraId}")
    suspend fun getDetallesByCompraId(
        @Header("X-Session-Id") sessionId: String,
        @Path("compraId") compraId: Long
    ): Response<List<DetalleCompraDTO>>
}