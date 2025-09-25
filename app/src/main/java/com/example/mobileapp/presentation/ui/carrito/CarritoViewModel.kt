package com.example.mobileapp.presentation.ui.carrito

import androidx.lifecycle.*
import com.example.mobileapp.data.remote.RetrofitClient
import com.example.mobileapp.data.remote.model.carrito.CarritoDTO
import com.example.mobileapp.data.repository.CarritoRepository
import kotlinx.coroutines.launch

class CarritoViewModel(private val repository: CarritoRepository) : ViewModel() {

    private val _carritoItems = MutableLiveData<List<CarritoItemUI>>()
    val carritoItems: LiveData<List<CarritoItemUI>> = _carritoItems

    private val _carritoCompleto = MutableLiveData<List<CarritoItemCompleto>>()
    val carritoCompleto: LiveData<List<CarritoItemCompleto>> = _carritoCompleto

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

                    // Crear items para UI básico
                    val uiItems = carritoItems.map { carritoDTO ->
                        CarritoItemUI(
                            idCarrito = carritoDTO.idCarrito,
                            idUsuario = carritoDTO.idUsuario,
                            idLibro = carritoDTO.idLibro,
                            cantidad = carritoDTO.cantidad,
                            precioUnitario = carritoDTO.precioUnitario,
                            tituloLibro = "Libro #${carritoDTO.idLibro}",
                            autorLibro = "Autor desconocido",
                            imagenPortada = null
                        )
                    }
                    _carritoItems.value = uiItems

                    // Obtener datos completos de libros para checkout
                    obtenerDatosCompletos(sessionId, carritoItems)
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

    private suspend fun obtenerDatosCompletos(sessionId: String, carritoItems: List<CarritoDTO>) {
        try {
            val itemsCompletos = carritoItems.map { item ->
                val libroResponse = RetrofitClient.libroApi.findById(sessionId, item.idLibro)
                val libro = if (libroResponse.isSuccessful) libroResponse.body() else null

                CarritoItemCompleto(
                    idCarrito = item.idCarrito,
                    idUsuario = item.idUsuario,
                    idLibro = item.idLibro,
                    cantidad = item.cantidad,
                    precioUnitario = item.precioUnitario ?: 0.0,
                    tituloLibro = libro?.titulo ?: "Libro #${item.idLibro}",
                    autorLibro = libro?.nombreCompletoAutor ?: "Autor desconocido"
                )
            }
            _carritoCompleto.value = itemsCompletos
        } catch (e: Exception) {
            _error.value = "Error obteniendo detalles: ${e.message}"
        }
    }

    fun actualizarItem(sessionId: String, itemId: Long, carritoDTO: CarritoDTO) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.updateCartItem(sessionId, itemId, carritoDTO)

                if (response.isSuccessful) {
                    cargarCarrito(sessionId)
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
                    cargarCarrito(sessionId)
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
                    _carritoCompleto.value = emptyList()
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

// Nuevo modelo para datos completos del carrito
data class CarritoItemCompleto(
    val idCarrito: Long? = null,
    val idUsuario: Long,
    val idLibro: Long,
    val cantidad: Int,
    val precioUnitario: Double,
    val tituloLibro: String,
    val autorLibro: String
)