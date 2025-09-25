package com.example.mobileapp.presentation.ui.libro

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mobileapp.R
import com.example.mobileapp.data.remote.RetrofitClient
import com.example.mobileapp.data.remote.SessionStore
import com.example.mobileapp.presentation.ui.genero.GenerosFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PagoConfirmacionFragment : Fragment(R.layout.fragment_pago_confirmacion) {

    private var compraId: Long = -1L
    private var totalAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            compraId = it.getLong(ARG_COMPRA_ID)
            totalAmount = it.getDouble(ARG_TOTAL_AMOUNT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvCompraId = view.findViewById<TextView>(R.id.tvCompraId)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotal)
        val tvEstado = view.findViewById<TextView>(R.id.tvEstado)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val btnVolverInicio = view.findViewById<Button>(R.id.btnVolverInicio)
        val btnConsultarEstado = view.findViewById<Button>(R.id.btnConsultarEstado)

        tvCompraId.text = "Compra ID: #$compraId"
        tvTotal.text = "Total: $${String.format("%.2f", totalAmount)}"
        tvEstado.text = "Estado: Procesando pago..."

        btnVolverInicio.setOnClickListener {
            // Limpiar stack y volver al inicio
            parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, GenerosFragment())
                .commit()
        }

        btnConsultarEstado.setOnClickListener {
            consultarEstadoPago(tvEstado, progressBar)
        }

        // Consultar estado automáticamente cada 10 segundos
        iniciarConsultaPeriodica(tvEstado, progressBar)
    }

    private fun iniciarConsultaPeriodica(tvEstado: TextView, progressBar: ProgressBar) {
        lifecycleScope.launch {
            repeat(6) { // 6 intentos = 1 minuto
                delay(10000) // Esperar 10 segundos
                consultarEstadoPago(tvEstado, progressBar, esSilencioso = true)
            }
        }
    }

    private fun consultarEstadoPago(
        tvEstado: TextView,
        progressBar: ProgressBar,
        esSilencioso: Boolean = false
    ) {
        lifecycleScope.launch {
            try {
                if (!esSilencioso) {
                    progressBar.visibility = View.VISIBLE
                }

                val sessionId = SessionStore.sessionId ?: ""
                val response = RetrofitClient.mercadoPagoApi.getPaymentStatus(sessionId, compraId)

                if (response.isSuccessful) {
                    val status = response.body()
                    val estado = status?.get("estado") as? String ?: "PENDIENTE"

                    val (texto, color) = when (estado) {
                        "PAGADO" -> "Estado: ✅ Pago confirmado" to android.R.color.holo_green_dark
                        "ENVIADO" -> "Estado: 📦 Preparando envío" to android.R.color.holo_blue_dark
                        "ENTREGADO" -> "Estado: ✅ Entregado" to android.R.color.holo_green_dark
                        else -> "Estado: ⏳ Pendiente de pago" to android.R.color.holo_orange_dark
                    }

                    tvEstado.text = texto
                    tvEstado.setTextColor(requireContext().getColor(color))

                } else {
                    if (!esSilencioso) {
                        tvEstado.text = "Estado: ❌ Error al consultar"
                        tvEstado.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                    }
                }
            } catch (e: Exception) {
                if (!esSilencioso) {
                    tvEstado.text = "Estado: ❌ Error de conexión"
                    tvEstado.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                }
            } finally {
                if (!esSilencioso) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        private const val ARG_COMPRA_ID = "compra_id"
        private const val ARG_TOTAL_AMOUNT = "total_amount"

        fun newInstance(compraId: Long, totalAmount: Double): PagoConfirmacionFragment {
            return PagoConfirmacionFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_COMPRA_ID, compraId)
                    putDouble(ARG_TOTAL_AMOUNT, totalAmount)
                }
            }
        }
    }
}