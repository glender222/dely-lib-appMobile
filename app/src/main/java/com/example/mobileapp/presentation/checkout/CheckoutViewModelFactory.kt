package com.example.mobileapp.presentation.checkout

import androidx.lifecycle.*
import com.example.mobileapp.data.remote.model.compra.CompraDTO
import com.example.mobileapp.data.remote.model.pago.MercadoPagoResponse
import com.example.mobileapp.data.repository.CompraRepository
import kotlinx.coroutines.launch

class PagoViewModel(private val repository: CompraRepository) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _compraCreada = MutableLiveData<CompraDTO?>()
    val compraCreada: LiveData<CompraDTO?> = _compraCreada

    private val _preferenceCreated = MutableLiveData<MercadoPagoResponse?>()
    val preferenceCreated: LiveData<MercadoPagoResponse?> = _preferenceCreated

    fun crearCompra(sessionId: String, compra: CompraDTO) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val response = repository.crearCompra(sessionId, compra)

                if (response.isSuccessful) {
                    _compraCreada.value = response.body()
                } else {
                    _error.value = "Error al crear compra: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun crearPreferenciaPago(sessionId: String, compraId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val response = repository.crearPreferenciaPago(sessionId, compraId)

                if (response.isSuccessful) {
                    _preferenceCreated.value = response.body()
                } else {
                    _error.value = "Error al crear preferencia de pago: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}