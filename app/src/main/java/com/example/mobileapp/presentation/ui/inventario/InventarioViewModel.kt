package com.example.mobileapp.presentation.ui.inventario

import androidx.lifecycle.*
import com.example.mobileapp.data.remote.RetrofitClient
import com.example.mobileapp.data.remote.model.inventario.InventarioDTO
import com.example.mobileapp.data.remote.model.inventario.InventarioConLibroDTO
import com.example.mobileapp.data.repository.InventarioRepository
import kotlinx.coroutines.launch

class InventarioViewModel(private val repository: InventarioRepository) : ViewModel() {

    private val _inventarios = MutableLiveData<List<InventarioConLibroDTO>>()
    val inventarios: LiveData<List<InventarioConLibroDTO>> = _inventarios

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _operationSuccess = MutableLiveData<String?>()
    val operationSuccess: LiveData<String?> = _operationSuccess

    fun cargarInventarios(sessionId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val response = repository.findAll(sessionId)

                if (response.isSuccessful) {
                    val inventariosBasicos = response.body() ?: emptyList()

                    // Enriquecer con datos del libro
                    val inventariosConLibro = inventariosBasicos.map { inventario ->
                        var tituloLibro: String? = null
                        var autorLibro: String? = null
                        var imagenPortada: String? = null

                        // Obtener datos del libro
                        try {
                            val libroResponse = RetrofitClient.libroApi.findById(sessionId, inventario.idLibro)
                            if (libroResponse.isSuccessful) {
                                val libro = libroResponse.body()
                                tituloLibro = libro?.titulo
                                autorLibro = libro?.nombreCompletoAutor
                                imagenPortada = libro?.imagenPortada
                            }
                        } catch (e: Exception) {
                            // Si falla obtener el libro, usar valores por defecto
                            tituloLibro = "Libro #${inventario.idLibro}"
                        }

                        InventarioConLibroDTO(
                            idInventario = inventario.idInventario,
                            idLibro = inventario.idLibro,
                            precio = inventario.precio,
                            cantidadStock = inventario.cantidadStock,
                            tituloLibro = tituloLibro ?: "Libro #${inventario.idLibro}",
                            autorLibro = autorLibro ?: "Autor desconocido",
                            imagenPortada = imagenPortada
                        )
                    }

                    _inventarios.value = inventariosConLibro
                } else {
                    _error.value = "Error al cargar inventarios: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun crearInventario(sessionId: String, inventario: InventarioDTO) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.createInventario(sessionId, inventario)

                if (response.isSuccessful) {
                    _operationSuccess.value = "Inventario creado exitosamente"
                    cargarInventarios(sessionId) // Recargar lista
                } else {
                    _error.value = "Error al crear inventario: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun actualizarInventario(sessionId: String, id: Long, inventario: InventarioDTO) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.updateInventario(sessionId, id, inventario)

                if (response.isSuccessful) {
                    _operationSuccess.value = "Inventario actualizado exitosamente"
                    cargarInventarios(sessionId) // Recargar lista
                } else {
                    _error.value = "Error al actualizar inventario: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun eliminarInventario(sessionId: String, id: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.deleteInventario(sessionId, id)

                if (response.isSuccessful) {
                    _operationSuccess.value = "Inventario eliminado exitosamente"
                    cargarInventarios(sessionId) // Recargar lista
                } else {
                    _error.value = "Error al eliminar inventario: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun buscarPorLibro(sessionId: String, libroId: Long): LiveData<InventarioDTO?> {
        val result = MutableLiveData<InventarioDTO?>()

        viewModelScope.launch {
            try {
                val response = repository.findByLibroId(sessionId, libroId)
                if (response.isSuccessful) {
                    result.value = response.body()
                } else {
                    result.value = null
                }
            } catch (e: Exception) {
                result.value = null
            }
        }

        return result
    }

    fun clearMessages() {
        _error.value = null
        _operationSuccess.value = null
    }
}