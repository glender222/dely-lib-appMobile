package com.example.mobileapp.data.repository

import com.example.mobileapp.data.remote.api.InventarioApi
import com.example.mobileapp.data.remote.model.inventario.InventarioDTO
import retrofit2.Response

class InventarioRepository(private val api: InventarioApi) {

    suspend fun createInventario(sessionId: String, inventario: InventarioDTO): Response<InventarioDTO> {
        return api.createInventario(sessionId, inventario)
    }

    suspend fun findAll(sessionId: String): Response<List<InventarioDTO>> {
        return api.findAll(sessionId)
    }

    suspend fun findById(sessionId: String, id: Long): Response<InventarioDTO> {
        return api.findById(sessionId, id)
    }

    suspend fun findByLibroId(sessionId: String, libroId: Long): Response<InventarioDTO> {
        return api.findByLibroId(sessionId, libroId)
    }

    suspend fun updateInventario(sessionId: String, id: Long, inventario: InventarioDTO): Response<InventarioDTO> {
        return api.updateInventario(sessionId, id, inventario)
    }

    suspend fun deleteInventario(sessionId: String, id: Long): Response<Unit> {
        return api.deleteInventario(sessionId, id)
    }
}