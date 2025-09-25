package com.example.mobileapp.presentation.checkout

import androidx.lifecycle.*
import com.example.mobileapp.data.remote.model.compra.CompraDTO
import com.example.mobileapp.data.repository.CarritoRepository
import com.example.mobileapp.data.repository.CompraRepository
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val carritoRepository: CarritoRepository,
    private val compraRepository: CompraRepository
) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _mercadoPagoUrl = MutableLiveData<String?>()
    val mercadoPagoUrl: LiveData<String?> = _mercadoPagoUrl

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> = _success

    fun procesarCompraConPago(
        sessionId: String,
        direccion: String,
        distrito: String,
        calle: String,
        ciudad: String,
        carritoItems: List<Any> // Usa el tipo que tengas definido
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                // 1. Crear compra
                val compraDTO = CompraDTO(
                    idUsuario = 0, // Se asigna en backend
                    direccionEnvio = direccion,
                    distrito = distrito,
                    calle = calle,
                    ciudad = ciudad,
                    estadoProcesoCompra = "PENDIENTE"
                )

                val compraResponse = compraRepository.crearCompra(sessionId, compraDTO)

                if (compraResponse.isSuccessful) {
                    val compraCreada = compraResponse.body()
                    val compraId = compraCreada?.idCompra

                    if (compraId != null) {
                        // 2. Crear preferencia de Mercado Pago
                        val preferenciaResponse = compraRepository.crearPreferenciaPago(sessionId, compraId)

                        if (preferenciaResponse.isSuccessful) {
                            val mercadoPago = preferenciaResponse.body()
                            _mercadoPagoUrl.value = mercadoPago?.initPoint
                        } else {
                            _error.value = "Error al crear preferencia de pago"
                        }
                    } else {
                        _error.value = "Error: No se pudo obtener ID de compra"
                    }
                } else {
                    _error.value = "Error al crear compra: ${compraResponse.message()}"
                }

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun limpiarCarrito(sessionId: String) {
        viewModelScope.launch {
            try {
                carritoRepository.clearCart(sessionId)
                _success.value = true
            } catch (e: Exception) {
                // Log error pero no mostrar al usuario
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _mercadoPagoUrl.value = null
    }
}