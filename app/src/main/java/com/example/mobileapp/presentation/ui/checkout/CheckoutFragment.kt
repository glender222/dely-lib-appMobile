package com.example.mobileapp.presentation.ui.checkout


import android.content.Context
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

import com.example.mobileapp.data.remote.model.pago.CompraConPagoRequest
import com.example.mobileapp.data.remote.model.pago.LibroCompraItem
import com.example.mobileapp.data.repository.CarritoRepository
import com.example.mobileapp.data.repository.PagoRepository
import com.example.mobileapp.presentation.ui.carrito.CarritoViewModel
import com.example.mobileapp.presentation.ui.carrito.CarritoViewModelFactory
import com.example.mobileapp.presentation.ui.pago.PagoViewModel
import com.example.mobileapp.presentation.ui.pago.PagoViewModelFactory
import kotlinx.coroutines.launch

class CheckoutFragment : Fragment(R.layout.fragment_checkout) {

    private val carritoViewModel: CarritoViewModel by viewModels {
        CarritoViewModelFactory(CarritoRepository(RetrofitClient.carritoApi))
    }

    private val pagoViewModel: PagoViewModel by viewModels {
        PagoViewModelFactory(PagoRepository(RetrofitClient.mercadoPagoApi, RetrofitClient.compraApi))
    }

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

        pagoViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnPagar.isEnabled = !isLoading
        }

        pagoViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        pagoViewModel.compraCreada.observe(viewLifecycleOwner) { response ->
            response?.let {
                val compra = it["compra"] as? Map<String, Any>
                val compraId = (compra?.get("idCompra") as? Double)?.toLong()

                if (compraId != null) {
                    // Crear preferencia de pago
                    pagoViewModel.crearPreferenciaPago(sessionId, compraId)
                } else {
                    Toast.makeText(requireContext(), "Error: No se pudo obtener ID de compra", Toast.LENGTH_LONG).show()
                }
            }
        }

        pagoViewModel.preferenceCreated.observe(viewLifecycleOwner) { preference ->
            preference?.let {
                // Abrir Mercado Pago en navegador
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.initPoint))
                startActivity(intent)

                Toast.makeText(requireContext(), "Redirigiendo a Mercado Pago...", Toast.LENGTH_SHORT).show()

                // Volver al carrito después de un momento
                view.postDelayed({
                    parentFragmentManager.popBackStack()
                }, 2000)
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

            val items = carritoViewModel.carritoItems.value
            if (items.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "El carrito está vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Convertir items del carrito a formato requerido
            val libros = items.mapNotNull { item ->
                val idLibro = item.idLibro
                val precio = item.precioUnitario
                if (idLibro != null && precio != null) {
                    LibroCompraItem(
                        idLibro = idLibro,
                        cantidad = item.cantidad,
                        precioUnitario = precio
                    )
                } else null
            }

            val compraRequest = CompraConPagoRequest(
                direccionEnvio = direccion,
                distrito = distrito,
                calle = calle,
                ciudad = ciudad,
                libros = libros
            )

            pagoViewModel.crearCompraConPago(sessionId, compraRequest)
        }
    }

    companion object {
        fun newInstance(): CheckoutFragment {
            return CheckoutFragment()
        }
    }
}