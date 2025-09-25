package com.example.mobileapp.presentation.ui.pago

import androidx.lifecycle.*
import com.example.mobileapp.data.remote.model.pago.CompraConPagoRequest
import com.example.mobileapp.data.remote.model.pago.MercadoPagoDTO
import com.example.mobileapp.data.repository.PagoRepository
import kotlinx.coroutines.launch

class PagoViewModel(private val repository: PagoRepository) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _compraCreada = MutableLiveData<Map<String, Any>?>()
    val compraCreada: LiveData<Map<String, Any>?> = _compraCreada

    private val _preferenceCreated = MutableLiveData<MercadoPagoDTO?>()
    val preferenceCreated: LiveData<MercadoPagoDTO?> = _preferenceCreated

    private val _paymentStatus = MutableLiveData<Map<String, Any>?>()
    val paymentStatus: LiveData<Map<String, Any>?> = _paymentStatus

    fun crearCompraConPago(sessionId: String, compra: CompraConPagoRequest) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val response = repository.createCompraWithPayment(sessionId, compra)

                if (response.isSuccessful) {
                    _compraCreada.value = response.body()
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Datos de compra inválidos"
                        401 -> "No autorizado"
                        403 -> "Sin permisos"
                        500 -> "Error del servidor"
                        else -> "Error al crear compra: ${response.code()}"
                    }
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
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

                val response = repository.createPreference(sessionId, compraId)

                if (response.isSuccessful) {
                    _preferenceCreated.value = response.body()
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Datos de pago inválidos"
                        401 -> "No autorizado"
                        403 -> "Sin permisos"
                        404 -> "Compra no encontrada"
                        500 -> "Error del servidor"
                        else -> "Error al crear preferencia de pago: ${response.code()}"
                    }
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun consultarEstadoPago(sessionId: String, compraId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val response = repository.getPaymentStatus(sessionId, compraId)

                if (response.isSuccessful) {
                    _paymentStatus.value = response.body()
                } else {
                    _error.value = "Error al consultar estado: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _compraCreada.value = null
        _preferenceCreated.value = null
        _paymentStatus.value = null
    }
}