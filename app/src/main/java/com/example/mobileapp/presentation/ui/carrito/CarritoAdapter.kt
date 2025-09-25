package com.example.mobileapp.presentation.ui.carrito

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.mobileapp.R
import com.example.mobileapp.data.remote.SessionStore

class CarritoAdapter(
    private val onLibroClick: (CarritoItemUI) -> Unit,
    private val onCantidadChanged: (CarritoItemUI, Int) -> Unit,
    private val onEliminar: (CarritoItemUI) -> Unit
) : ListAdapter<CarritoItemUI, CarritoAdapter.CarritoViewHolder>(CarritoDiffCallback()) {

    inner class CarritoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPortada: ImageView = view.findViewById(R.id.ivPortada)
        val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
        val tvAutor: TextView = view.findViewById(R.id.tvAutor)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val tvCantidad: TextView = view.findViewById(R.id.tvCantidad)
        val btnDecrementar: ImageButton = view.findViewById(R.id.btnDecrementar)
        val btnIncrementar: ImageButton = view.findViewById(R.id.btnIncrementar)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)
        val tvSubtotal: TextView = view.findViewById(R.id.tvSubtotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = getItem(position)

        // Datos básicos
        holder.tvTitulo.text = item.tituloLibro ?: "Libro #${item.idLibro}"
        holder.tvAutor.text = item.autorLibro ?: "Autor desconocido"
        holder.tvPrecio.text = "$${String.format("%.2f", item.precioUnitario ?: 0.0)}"
        holder.tvCantidad.text = item.cantidad.toString()

        // Subtotal
        val subtotal = (item.precioUnitario ?: 0.0) * item.cantidad
        holder.tvSubtotal.text = "Subtotal: $${String.format("%.2f", subtotal)}"

        // Imagen de portada
        val sessionId = SessionStore.sessionId ?: ""
        val imageUrl = when {
            !item.imagenPortada.isNullOrBlank() && (item.imagenPortada.startsWith("http") || item.imagenPortada.startsWith("/")) -> {
                if (item.imagenPortada.startsWith("/")) "http://10.0.2.2:9090" + item.imagenPortada else item.imagenPortada
            }
            item.idLibro != null -> "http://10.0.2.2:9090/api/v1/libros/${item.idLibro}/imagen"
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

        Glide.with(holder.itemView.context)
            .load(model)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.ivPortada)

        // Click en el libro para ver detalle
        holder.itemView.setOnClickListener {
            onLibroClick(item)
        }

        // Botones de cantidad
        holder.btnDecrementar.setOnClickListener {
            val nuevaCantidad = item.cantidad - 1
            if (nuevaCantidad > 0) {
                onCantidadChanged(item, nuevaCantidad)
            }
        }

        holder.btnIncrementar.setOnClickListener {
            val nuevaCantidad = item.cantidad + 1
            onCantidadChanged(item, nuevaCantidad)
        }

        // Botón eliminar
        holder.btnEliminar.setOnClickListener {
            onEliminar(item)
        }
    }

    private class CarritoDiffCallback : DiffUtil.ItemCallback<CarritoItemUI>() {
        override fun areItemsTheSame(oldItem: CarritoItemUI, newItem: CarritoItemUI): Boolean {
            return oldItem.idCarrito == newItem.idCarrito
        }

        override fun areContentsTheSame(oldItem: CarritoItemUI, newItem: CarritoItemUI): Boolean {
            return oldItem == newItem
        }
    }
}