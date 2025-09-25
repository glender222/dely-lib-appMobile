package com.example.mobileapp.presentation.ui.carrito

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.data.remote.RetrofitClient
import com.example.mobileapp.data.remote.SessionStore
import com.example.mobileapp.data.remote.model.carrito.CarritoDTO
import com.example.mobileapp.data.repository.CarritoRepository
import com.example.mobileapp.presentation.ui.checkout.CheckoutFragment
import com.example.mobileapp.presentation.ui.libro.LibroDetalleFragment
import kotlinx.coroutines.launch

class CarritoFragment : Fragment(R.layout.fragment_carrito) {

    private val viewModel: CarritoViewModel by viewModels {
        CarritoViewModelFactory(CarritoRepository(RetrofitClient.carritoApi))
    }

    private lateinit var adapter: CarritoAdapter
    private lateinit var tvTotal: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var rvCarrito: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        tvTotal = view.findViewById(R.id.tvTotal)
        tvEmpty = view.findViewById(R.id.tvEmptyCarrito)
        rvCarrito = view.findViewById(R.id.rvCarrito)
        val btnProcederPago = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnProcederPago)
        val btnLimpiarCarrito = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLimpiarCarrito)

        // Verificar que es cliente
        val prefs = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val userRole = prefs.getString("USER_ROLE", "") ?: ""
        val isCliente = "CLIENTE".equals(userRole.trim(), ignoreCase = true)

        if (!isCliente) {
            Toast.makeText(requireContext(), "Solo los clientes pueden acceder al carrito", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }

        setupRecyclerView()
        setupObservers()
        setupClickListeners(btnBack, btnProcederPago, btnLimpiarCarrito)

        // Cargar carrito
        cargarCarrito()
    }

    private fun setupRecyclerView() {
        adapter = CarritoAdapter(
            onLibroClick = { carritoItem ->
                // Navegar al detalle del libro
                carritoItem.idLibro?.let { libroId ->
                    val detalleFragment = LibroDetalleFragment.newInstance(libroId)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, detalleFragment)
                        .addToBackStack(null)
                        .commit()
                }
            },
            onCantidadChanged = { carritoItem, nuevaCantidad ->
                actualizarCantidad(carritoItem, nuevaCantidad)
            },
            onEliminar = { carritoItem ->
                eliminarItem(carritoItem)
            }
        )

        rvCarrito.layoutManager = LinearLayoutManager(requireContext())
        rvCarrito.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.carritoItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            actualizarUI(items)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Mostrar/ocultar loading si tienes un ProgressBar
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupClickListeners(
        btnBack: ImageButton,
        btnProcederPago: com.google.android.material.button.MaterialButton,
        btnLimpiarCarrito: com.google.android.material.button.MaterialButton
    ) {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnProcederPago.setOnClickListener {
            val items = viewModel.carritoItems.value
            if (items.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "El carrito está vacío", Toast.LENGTH_SHORT).show()
            } else {
                // Navegar a pantalla de checkout/pago
                val fragment = CheckoutFragment.newInstance()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        btnLimpiarCarrito.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Limpiar carrito")
                .setMessage("¿Estás seguro de que deseas eliminar todos los items del carrito?")
                .setPositiveButton("Sí") { _, _ ->
                    limpiarCarrito()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun actualizarUI(items: List<CarritoItemUI>) {
        if (items.isEmpty()) {
            rvCarrito.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvTotal.text = "Total: $0.00"
        } else {
            rvCarrito.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE

            val total = items.sumOf { (it.precioUnitario ?: 0.0) * it.cantidad }
            tvTotal.text = "Total: $${String.format("%.2f", total)}"
        }
    }




    private fun cargarCarrito() {
        val sessionId = SessionStore.sessionId ?: ""
        viewModel.cargarCarrito(sessionId)
    }

    private fun actualizarCantidad(item: CarritoItemUI, nuevaCantidad: Int) {
        if (nuevaCantidad <= 0) {
            eliminarItem(item)
            return
        }

        // **AQUÍ ESTÁ LA CORRECCIÓN DEL ERROR**
        val sessionId = SessionStore.sessionId ?: ""
        val libroId = item.idLibro ?: return // Si es null, no hacer nada

        val carritoDTO = CarritoDTO(
            idCarrito = item.idCarrito,
            idUsuario = item.idUsuario ?: return, // Si es null, no hacer nada
            idLibro = libroId, // Ahora es Long, no Long?
            cantidad = nuevaCantidad,
            precioUnitario = item.precioUnitario
        )

        item.idCarrito?.let { id ->
            viewModel.actualizarItem(sessionId, id, carritoDTO)
        }
    }

    private fun eliminarItem(item: CarritoItemUI) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar item")
            .setMessage("¿Deseas eliminar '${item.tituloLibro}' del carrito?")
            .setPositiveButton("Eliminar") { _, _ ->
                val sessionId = SessionStore.sessionId ?: ""
                item.idCarrito?.let { id ->
                    viewModel.eliminarItem(sessionId, id)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun limpiarCarrito() {
        val sessionId = SessionStore.sessionId ?: ""
        viewModel.limpiarCarrito(sessionId)
    }

    companion object {
        fun newInstance(): CarritoFragment {
            return CarritoFragment()
        }
    }
}