package com.example.mobileapp.presentation.ui.inventario

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
import com.example.mobileapp.data.remote.model.inventario.InventarioConLibroDTO

class InventarioAdapter(
    private val onEditClick: (InventarioConLibroDTO) -> Unit,
    private val onDeleteClick: (InventarioConLibroDTO) -> Unit
) : ListAdapter<InventarioConLibroDTO, InventarioAdapter.InventarioViewHolder>(InventarioDiffCallback()) {

    inner class InventarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPortada: ImageView = view.findViewById(R.id.ivPortada)
        val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
        val tvAutor: TextView = view.findViewById(R.id.tvAutor)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val tvStock: TextView = view.findViewById(R.id.tvStock)
        val tvEstadoStock: TextView = view.findViewById(R.id.tvEstadoStock)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventario, parent, false)
        return InventarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventarioViewHolder, position: Int) {
        val item = getItem(position)

        // Datos básicos
        holder.tvTitulo.text = item.tituloLibro ?: "Libro #${item.idLibro}"
        holder.tvAutor.text = item.autorLibro ?: "Autor desconocido"
        holder.tvPrecio.text = "$${String.format("%.2f", item.precio)}"
        holder.tvStock.text = "Stock: ${item.cantidadStock}"

        // Estado del stock
        when {
            item.cantidadStock <= 0 -> {
                holder.tvEstadoStock.text = "Sin stock"
                holder.tvEstadoStock.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
            }
            item.cantidadStock <= 5 -> {
                holder.tvEstadoStock.text = "Stock bajo"
                holder.tvEstadoStock.setTextColor(holder.itemView.context.getColor(android.R.color.holo_orange_dark))
            }
            else -> {
                holder.tvEstadoStock.text = "Disponible"
                holder.tvEstadoStock.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
            }
        }

        // Imagen de portada
        val sessionId = SessionStore.sessionId ?: ""
        val imageUrl = when {
            !item.imagenPortada.isNullOrBlank() && (item.imagenPortada.startsWith("http") || item.imagenPortada.startsWith("/")) -> {
                if (item.imagenPortada.startsWith("/")) "http://10.0.2.2:9090" + item.imagenPortada else item.imagenPortada
            }
            else -> "http://10.0.2.2:9090/api/v1/libros/${item.idLibro}/imagen"
        }

        val model = GlideUrl(
            imageUrl,
            LazyHeaders.Builder()
                .addHeader("X-Session-Id", sessionId)
                .build()
        )

        Glide.with(holder.itemView.context)
            .load(model)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.ivPortada)

        // Botones de acción
        holder.btnEditar.setOnClickListener {
            onEditClick(item)
        }

        holder.btnEliminar.setOnClickListener {
            onDeleteClick(item)
        }
    }

    private class InventarioDiffCallback : DiffUtil.ItemCallback<InventarioConLibroDTO>() {
        override fun areItemsTheSame(oldItem: InventarioConLibroDTO, newItem: InventarioConLibroDTO): Boolean {
            return oldItem.idInventario == newItem.idInventario
        }

        override fun areContentsTheSame(oldItem: InventarioConLibroDTO, newItem: InventarioConLibroDTO): Boolean {
            return oldItem == newItem
        }
    }
}