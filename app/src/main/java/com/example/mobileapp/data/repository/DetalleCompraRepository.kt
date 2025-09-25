package com.example.mobileapp.data.repository

import com.example.mobileapp.data.remote.api.DetalleCompraApi
import com.example.mobileapp.data.remote.model.compra.DetalleCompraDTO
import retrofit2.Response

class DetalleCompraRepository(
    private val detalleCompraApi: DetalleCompraApi
) {

    suspend fun crearDetalleCompra(sessionId: String, detalle: DetalleCompraDTO): Response<DetalleCompraDTO> {
        return detalleCompraApi.crearDetalleCompra(sessionId, detalle)
    }

    suspend fun obtenerDetallesPorCompra(sessionId: String, compraId: Long): Response<List<DetalleCompraDTO>> {
        return detalleCompraApi.getDetallesByCompraId(sessionId, compraId)
    }
}