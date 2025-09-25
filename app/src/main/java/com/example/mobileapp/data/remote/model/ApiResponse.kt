package com.example.mobileapp.data.remote.model


data class ApiResponse<T>(
    val success: Boolean? = null,
    val error: Boolean? = null,
    val message: String? = null,
    val data: T? = null,
    val timestamp: Long? = null
)