package com.example.mobileapp.presentation.ui.carrito

import androidx.lifecycle.*
import com.example.mobileapp.data.remote.model.carrito.CarritoDTO
import com.example.mobileapp.data.repository.CarritoRepository
import kotlinx.coroutines.launch

class CarritoViewModel(private val repository: CarritoRepository) : ViewModel() {

    private val _carritoItems = MutableLiveData<List<CarritoItemUI>>()
    val carritoItems: LiveData<List<CarritoItemUI>> = _carritoItems

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun cargarCarrito(sessionId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val response = repository.getMyCart(sessionId)

                if (response.isSuccessful) {
                    val carritoItems = response.body() ?: emptyList()
                    val uiItems = carritoItems.map { carritoDTO ->
                        CarritoItemUI(
                            idCarrito = carritoDTO.idCarrito,
                            idUsuario = carritoDTO.idUsuario,
                            idLibro = carritoDTO.idLibro,
                            cantidad = carritoDTO.cantidad,
                            precioUnitario = carritoDTO.precioUnitario,
                            // Datos básicos por ahora - puedes expandir obteniendo datos del libro
                            tituloLibro = "Libro #${carritoDTO.idLibro}",
                            autorLibro = "Autor desconocido",
                            imagenPortada = null
                        )
                    }
                    _carritoItems.value = uiItems
                } else {
                    _error.value = "Error al cargar carrito: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun actualizarItem(sessionId: String, itemId: Long, carritoDTO: CarritoDTO) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.updateCartItem(sessionId, itemId, carritoDTO)

                if (response.isSuccessful) {
                    cargarCarrito(sessionId) // Recargar para actualizar la UI
                } else {
                    _error.value = "Error al actualizar: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun eliminarItem(sessionId: String, itemId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.removeFromCart(sessionId, itemId)

                if (response.isSuccessful) {
                    cargarCarrito(sessionId) // Recargar para actualizar la UI
                } else {
                    _error.value = "Error al eliminar: ${response.code()}"
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
                _loading.value = true
                val response = repository.clearCart(sessionId)

                if (response.isSuccessful) {
                    _carritoItems.value = emptyList()
                } else {
                    _error.value = "Error al limpiar carrito: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}