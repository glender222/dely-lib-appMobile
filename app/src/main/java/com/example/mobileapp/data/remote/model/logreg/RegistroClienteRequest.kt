package com.example.mobileapp.data.model

data class RegistroClienteRequest(
    val nombreCompleto: String,
    val email: String,
    val password: String,
    val fechaNacimiento: String
)
