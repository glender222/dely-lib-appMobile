package com.example.mobileapp.presentation.ui.libro

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.mobileapp.R
import com.example.mobileapp.data.remote.RetrofitClient
import com.example.mobileapp.data.remote.SessionStore
import com.example.mobileapp.data.remote.model.LibroDTO
import com.example.mobileapp.data.remote.model.carrito.CarritoDTO
import com.example.mobileapp.data.repository.CarritoRepository
import kotlinx.coroutines.launch

class LibroDetalleFragment : Fragment(R.layout.fragment_libro_detalle) {

    private var libroId: Long = -1L
    private lateinit var libro: LibroDTO
    private lateinit var carritoRepository: CarritoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            libroId = it.getLong(ARG_LIBRO_ID, -1L)
        }
        carritoRepository = CarritoRepository(RetrofitClient.carritoApi)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val ivPortada = view.findViewById<ImageView>(R.id.ivPortada)
        val tvTitulo = view.findViewById<TextView>(R.id.tvTitulo)
        val tvAutor = view.findViewById<TextView>(R.id.tvAutor)
        val tvSinopsis = view.findViewById<TextView>(R.id.tvSinopsis)
        val tvEditorial = view.findViewById<TextView>(R.id.tvEditorial)
        val tvIsbn = view.findViewById<TextView>(R.id.tvIsbn)
        val tvFechaLanzamiento = view.findViewById<TextView>(R.id.tvFechaLanzamiento)
        val tvIdioma = view.findViewById<TextView>(R.id.tvIdioma)
        val tvNumPaginas = view.findViewById<TextView>(R.id.tvNumPaginas)
        val tvEdicion = view.findViewById<TextView>(R.id.tvEdicion)
        val tvEstrellas = view.findViewById<TextView>(R.id.tvEstrellas)
        val btnAgregarCarrito = view.findViewById<Button>(R.id.btnAgregarCarrito)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Verificar rol para mostrar/ocultar botón carrito
        val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val userRole = prefs.getString("USER_ROLE", "") ?: ""
        val isCliente = "CLIENTE".equals(userRole.trim(), ignoreCase = true)

        btnAgregarCarrito.visibility = if (isCliente) View.VISIBLE else View.GONE

        // Cargar detalles del libro
        cargarLibro(libroId)

        // Acción agregar al carrito
        btnAgregarCarrito.setOnClickListener {
            agregarAlCarrito()
        }
    }

    private fun cargarLibro(id: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sessionId = SessionStore.sessionId ?: ""
                val response = RetrofitClient.libroApi.findById(sessionId, id)

                if (response.isSuccessful && response.body() != null) {
                    libro = response.body()!!
                    mostrarDetallesLibro()
                } else {
                    Toast.makeText(requireContext(), "Error al cargar libro: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDetallesLibro() {
        val view = requireView()
        val sessionId = SessionStore.sessionId ?: ""

        // Imagen
        val ivPortada = view.findViewById<ImageView>(R.id.ivPortada)
        val imageUrl = when {
            !libro.imagenPortada.isNullOrBlank() && (libro.imagenPortada!!.startsWith("http") || libro.imagenPortada!!.startsWith("/")) -> {
                if (libro.imagenPortada!!.startsWith("/")) "http://10.0.2.2:9090" + libro.imagenPortada else libro.imagenPortada
            }
            libro.idLibro != null -> "http://10.0.2.2:9090/api/v1/libros/${libro.idLibro}/imagen"
            else -> null
        }

        val model = imageUrl?.let {
            GlideUrl(
                it,
                LazyHeaders.Builder()
                    .addHeader("X-Session-Id", sessionId)
                    .build()
            )
        }

        Glide.with(this)
            .load(model)
            .placeholder(R.drawable.ic_placeholder)
            .into(ivPortada)

        // Información básica
        view.findViewById<TextView>(R.id.tvTitulo).text = libro.titulo
        view.findViewById<TextView>(R.id.tvAutor).text = libro.nombreCompletoAutor ?: "Autor desconocido"
        view.findViewById<TextView>(R.id.tvSinopsis).text = libro.sinopsis ?: "Sin sinopsis disponible"

        // Información adicional
        view.findViewById<TextView>(R.id.tvEditorial).text = "Editorial: ${libro.editorial ?: "No especificada"}"
        view.findViewById<TextView>(R.id.tvIsbn).text = "ISBN: ${libro.isbn ?: "No disponible"}"
        view.findViewById<TextView>(R.id.tvFechaLanzamiento).text = "Fecha: ${libro.fechaLanzamiento ?: "No especificada"}"
        view.findViewById<TextView>(R.id.tvIdioma).text = "Idioma: ${libro.idioma ?: "No especificado"}"
        view.findViewById<TextView>(R.id.tvNumPaginas).text = "Páginas: ${libro.numPaginas ?: 0}"
        view.findViewById<TextView>(R.id.tvEdicion).text = "Edición: ${libro.edicion ?: "No especificada"}"

        // Puntuación
        val rating = (libro.puntuacionPromedio ?: 0.0).coerceIn(0.0, 5.0)
        val filled = rating.toInt()
        val half = (rating - filled) >= 0.5
        val stars = StringBuilder().apply {
            repeat(filled) { append('★') }
            if (half) append('★')
            val remaining = 5 - length
            repeat(remaining) { append('☆') }
        }.toString()
        view.findViewById<TextView>(R.id.tvEstrellas).text = "$stars (${String.format("%.1f", rating)})"
    }

    private fun agregarAlCarrito() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val sessionId = SessionStore.sessionId ?: ""
                val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val userId = prefs.getString("USER_ID", "")?.toLongOrNull()

                if (userId == null) {
                    Toast.makeText(requireContext(), "Error: Usuario no válido", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val carritoItem = CarritoDTO(
                    idUsuario = userId,
                    idLibro = libro.idLibro!!,
                    cantidad = 1
                )

                val response = carritoRepository.addToCart(sessionId, carritoItem)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Libro agregado al carrito", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    Toast.makeText(requireContext(), "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val ARG_LIBRO_ID = "arg_libro_id"

        fun newInstance(libroId: Long): LibroDetalleFragment {
            val fragment = LibroDetalleFragment()
            val args = Bundle()
            args.putLong(ARG_LIBRO_ID, libroId)
            fragment.arguments = args
            return fragment
        }
    }
}