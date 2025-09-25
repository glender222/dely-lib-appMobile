package com.example.mobileapp.data.remote.model.logreg

data class LoginResponse(
    val sessionId: String,
    val userId: Long,
    val nombre: String,
    val rol: String
)
