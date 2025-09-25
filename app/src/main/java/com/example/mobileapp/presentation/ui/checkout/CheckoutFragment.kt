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

        // Cargar carrito y calcular total
        val sessionId = SessionStore.sessionId ?: ""
        carritoViewModel.cargarCarrito(sessionId)

        carritoViewModel.carritoItems.observe(viewLifecycleOwner) { items ->
            val total = items.sumOf { (it.precioUnitario ?: 0.0) * it.cantidad }
            tvTotal.text = "Total: $${String.format("%.2f", total)}"
        }

        carritoViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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

            val items = carritoViewModel.carritoItems.value
            if (items.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "El carrito está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Procesar pago sin ViewModels adicionales
            procesarPago(sessionId, direccion, distrito, calle, ciudad, items, progressBar, btnPagar)
        }
    }

    private fun procesarPago(
        sessionId: String,
        direccion: String,
        distrito: String,
        calle: String,
        ciudad: String,
        items: List<com.example.mobileapp.data.remote.model.carrito.CarritoDTO>,
        progressBar: ProgressBar,
        btnPagar: Button
    ) {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                btnPagar.isEnabled = false

                // 1. Crear compra (SIN idUsuario - el backend lo manejará automáticamente)
                val compraDTO = CompraDTO(
                    idUsuario = 0L, // El backend ignorará este valor y usará el sessionId
                    direccionEnvio = direccion,
                    distrito = distrito,
                    calle = calle,
                    ciudad = ciudad,
                    estadoProcesoCompra = "PENDIENTE"
                )

                val compraResponse = compraRepository.crearCompra(sessionId, compraDTO)
                if (!compraResponse.isSuccessful) {
                    val errorMsg = "Error al crear compra: ${compraResponse.code()}"
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                    return@launch
                }

                val compraCreada = compraResponse.body()!!
                val compraId = compraCreada.idCompra!!

                // 2. Crear detalles de compra
                for (item in items) {
                    val detalleDTO = DetalleCompraDTO(
                        idCompra = compraId,
                        idLibro = item.idLibro!!,
                        cantidad = item.cantidad,
                        precioUnitario = item.precioUnitario!!,
                        subtotal = item.precioUnitario!! * item.cantidad
                    )

                    val detalleResponse = detalleCompraRepository.crearDetalleCompra(sessionId, detalleDTO)
                    if (!detalleResponse.isSuccessful) {
                        val errorMsg = "Error al crear detalle: ${detalleResponse.code()}"
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                        return@launch
                    }
                }

                // 3. Crear preferencia de pago en Mercado Pago
                val preferenceResponse = compraRepository.crearPreferenciaPago(sessionId, compraId)
                if (!preferenceResponse.isSuccessful) {
                    val errorMsg = "Error al crear preferencia de pago: ${preferenceResponse.code()}"
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                    return@launch
                }

                val preference = preferenceResponse.body()!!

                // 4. Abrir Mercado Pago en navegador
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(preference.initPoint))
                startActivity(intent)

                Toast.makeText(requireContext(), "Redirigiendo a Mercado Pago...", Toast.LENGTH_SHORT).show()

                // 5. Limpiar carrito después del pago exitoso
                carritoViewModel.limpiarCarrito(sessionId)

                // 6. Volver al carrito después de un momento
                view?.postDelayed({
                    parentFragmentManager.popBackStack()
                }, 2000)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
                btnPagar.isEnabled = true
            }
        }
    }

    companion object {
        fun newInstance(): CheckoutFragment {
            return CheckoutFragment()
        }
    }
}