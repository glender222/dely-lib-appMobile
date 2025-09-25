package com.example.mobileapp.data.remote.model.genero

import com.google.gson.annotations.SerializedName

data class GeneroLibroDTO(
    @SerializedName(
        value = "idGeneroLibros",
        alternate = [
            "id_genero_libros",
            "id",
            // variantes comunes singulares/plurales desde backend
            "idGeneroLibro",
            "id_genero_libro"
        ]
    )
    val idGeneroLibros: Long? = null,
    @SerializedName(value = "idGenero", alternate = ["id_genero"])
    val idGenero: Long,
    @SerializedName(value = "idLibro", alternate = ["id_libro"])
    val idLibro: Long,
    @SerializedName(value = "estado", alternate = ["status"])
    val estado: String? = null
)
