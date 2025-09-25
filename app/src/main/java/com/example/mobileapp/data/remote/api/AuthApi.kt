package com.example.mobileapp.data.remote.api

import com.example.mobileapp.data.model.RegistroClienteRequest
import com.example.mobileapp.data.remote.model.logreg.LoginRequest
import com.example.mobileapp.data.remote.model.logreg.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/v1/register")
    suspend fun register(@Body request: RegistroClienteRequest): Response<Long>

}
