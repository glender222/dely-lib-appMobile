package com.example.mobileapp.data.remote.model

data class LibroDTO(
    val idLibro: Long?,
    val titulo: String,
    val puntuacionPromedio: Double?,
    val sinopsis: String?,
    val fechaLanzamiento: String?,
    val isbn: String?,
    val edicion: String?,
    val editorial: String?,
    val idioma: String?,
    val numPaginas: Int?,
    val nombreCompletoAutor: String?,
    val imagenPortada: String?
)
