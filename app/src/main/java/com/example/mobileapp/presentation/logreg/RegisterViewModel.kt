package com.example.mobileapp.presentation.logreg

import androidx.lifecycle.*
import com.example.mobileapp.data.model.RegistroClienteRequest
import com.example.mobileapp.data.repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<Response<Long>>()
    val registerResult: LiveData<Response<Long>> get() = _registerResult

    fun register(nombre: String, apellido: String, email: String, password: String, fecha: String) {
        viewModelScope.launch {
            val request = RegistroClienteRequest(
                nombreCompleto = "$nombre $apellido",
                email = email,
                password = password,
                fechaNacimiento = fecha // asegúrate que sea yyyy-MM-dd
            )
            try {
                val response = repository.registerCliente(request)
                _registerResult.value = response
            } catch (e: Exception) {
                _registerResult.value = null
            }
        }
    }
}
