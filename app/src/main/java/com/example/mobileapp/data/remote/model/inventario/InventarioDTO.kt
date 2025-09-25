package com.example.mobileapp.data.remote.model.inventario

import com.google.gson.annotations.SerializedName

data class InventarioDTO(
    @SerializedName("idInventario")
    val idInventario: Long? = null,

    @SerializedName("idLibro")
    val idLibro: Long,

    @SerializedName("precio")
    val precio: Double,

    @SerializedName("cantidadStock")
    val cantidadStock: Int
)

// Modelo extendido para UI que incluye datos del libro
data class InventarioConLibroDTO(
    val idInventario: Long? = null,
    val idLibro: Long,
    val precio: Double,
    val cantidadStock: Int,
    // Datos del libro para mostrar en UI
    val tituloLibro: String? = null,
    val autorLibro: String? = null,
    val imagenPortada: String? = null
)