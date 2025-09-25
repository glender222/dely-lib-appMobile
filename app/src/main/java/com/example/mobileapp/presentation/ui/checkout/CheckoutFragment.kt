package com.example.mobileapp.presentation.ui.checkout

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mobileapp.R
import com.example.mobileapp.data.remote.RetrofitClient
import com.example.mobileapp.data.remote.SessionStore
import com.example.mobileapp.data.remote.model.compra.CompraDTO
import com.example.mobileapp.data.remote.model.compra.DetalleCompraDTO
import com.example.mobileapp.data.repository.CarritoRepository
import com.example.mobileapp.data.repository.CompraRepository
import com.example.mobileapp.data.repository.DetalleCompraRepository
import com.example.mobileapp.presentation.ui.carrito.CarritoItemCompleto
import com.example.mobileapp.presentation.ui.carrito.CarritoViewModel
import com.example.mobileapp.presentation.ui.carrito.CarritoViewModelFactory
import kotlinx.coroutines.launch

class CheckoutFragment : Fragment(R.layout.fragment_checkout) {

    private val carritoViewModel: CarritoViewModel by viewModels {
        CarritoViewModelFactory(CarritoRepository(RetrofitClient.carritoApi))
    }

    private val compraRepository = CompraRepository(RetrofitClient.compraApi, RetrofitClient.mercadoPagoApi)
    private val detalleCompraRepository = DetalleCompraRepository(RetrofitClient.detalleCompraApi)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val etDireccion = view.findViewById<EditText>(R.id.etDireccion)
        val etDistrito = view.findViewById<EditText>(R.id.etDistrito)
        val etCalle = view.findViewById<EditText>(R.id.etCalle)
        val etCiudad = view.findViewById<EditText>(R.id.etCiudad)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotal)
        val btnPagar = view.findViewById<Button>(R.id.btnPagar)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Cargar carrito completo
        val sessionId = SessionStore.sessionId ?: ""
        carritoViewModel.cargarCarrito(sessionId)

        // Observar carrito completo (con datos de libros)
        carritoViewModel.carritoCompleto.observe(viewLifecycleOwner) { items ->
            val total = items.sumOf { it.precioUnitario * it.cantidad }
            tvTotal.text = "Total: $${String.format("%.2f", total)}"
        }

        carritoViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        carritoViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        btnPagar.setOnClickListener {
            val direccion = etDireccion.text.toString().trim()
            val distrito = etDistrito.text.toString().trim()
            val calle = etCalle.text.toString().trim()
            val ciudad = etCiudad.text.toString().trim()

            if (direccion.isEmpty() || distrito.isEmpty() || calle.isEmpty() || ciudad.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val items = carritoViewModel.carritoCompleto.value
            if (items.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "El carrito está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            procesarPago(sessionId, direccion, distrito, calle, ciudad, items, progressBar, btnPagar)
        }
    }

    private fun procesarPago(
        sessionId: String,
        direccion: String,
        distrito: String,
        calle: String,
        ciudad: String,
        items: List<CarritoItemCompleto>,
        progressBar: ProgressBar,
        btnPagar: Button
    ) {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                btnPagar.isEnabled = false
                btnPagar.text = "Procesando..."

                // 1. Crear compra
                val compraDTO = CompraDTO(
                    idUsuario = 0L, // El backend lo maneja automáticamente
                    direccionEnvio = direccion,
                    distrito = distrito,
                    calle = calle,
                    ciudad = ciudad,
                    estadoProcesoCompra = "PENDIENTE"
                )

                val compraResponse = compraRepository.crearCompra(sessionId, compraDTO)
                if (!compraResponse.isSuccessful) {
                    mostrarError("Error al crear compra: ${compraResponse.code()}")
                    return@launch
                }

                val compraCreada = compraResponse.body()!!
                val compraId = compraCreada.idCompra!!

                // 2. Crear detalles de compra
                for (item in items) {
                    val detalleDTO = DetalleCompraDTO(
                        idCompra = compraId,
                        idLibro = item.idLibro,
                        cantidad = item.cantidad,
                        precioUnitario = item.precioUnitario,
                        subtotal = item.precioUnitario * item.cantidad
                    )

                    val detalleResponse = detalleCompraRepository.crearDetalleCompra(sessionId, detalleDTO)
                    if (!detalleResponse.isSuccessful) {
                        mostrarError("Error al crear detalle: ${detalleResponse.code()}")
                        return@launch
                    }
                }

                // 3. Crear preferencia de pago en Mercado Pago
                val preferenciaResponse = compraRepository.crearPreferenciaPago(sessionId, compraId)
                if (!preferenciaResponse.isSuccessful) {
                    mostrarError("Error al crear preferencia de pago: ${preferenciaResponse.code()}")
                    return@launch
                }

                val preference = preferenciaResponse.body()!!

                // 4. Abrir Mercado Pago en navegador
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(preference.initPoint))
                startActivity(intent)

                Toast.makeText(requireContext(), "Redirigiendo a Mercado Pago...", Toast.LENGTH_SHORT).show()

                // 5. Limpiar carrito después del pago exitoso
                carritoViewModel.limpiarCarrito(sessionId)

                // 6. Navegar a confirmación
                val fragment = PagoConfirmacionFragment.newInstance(compraId, preference.totalAmount)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()

            } catch (e: Exception) {
                mostrarError("Error inesperado: ${e.message}")
            } finally {
                progressBar.visibility = View.GONE
                btnPagar.isEnabled = true
                btnPagar.text = "Pagar"
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
    }

    companion object {
        fun newInstance(): CheckoutFragment {
            return CheckoutFragment()
        }
    }
}