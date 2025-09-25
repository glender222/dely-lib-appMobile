package com.example.mobileapp.presentation.ui.libro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.mobileapp.R
import com.example.mobileapp.data.remote.model.LibroDTO
import android.widget.ImageButton

class LibroAdapter(
    private val sessionId: String,
    private val onClick: (LibroDTO) -> Unit,
    private val onDelete: ((LibroDTO) -> Unit)? = null,
    private val onRemoveFromGenero: ((LibroDTO) -> Unit)? = null
) : RecyclerView.Adapter<LibroAdapter.LibroVH>() {

    private val data = mutableListOf<LibroDTO>()
    var deleteMode: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun submit(items: List<LibroDTO>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    inner class LibroVH(view: View) : RecyclerView.ViewHolder(view) {
        val portada: ImageView = view.findViewById(R.id.ivPortada)
        val titulo: TextView = view.findViewById(R.id.tvTitulo)
        val autor: TextView = view.findViewById(R.id.tvAutor)
        val sinopsis: TextView = view.findViewById(R.id.tvSinopsis)
        val estrellas: TextView = view.findViewById(R.id.tvEstrellas)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminarLibro)
        val btnQuitarDeGenero: ImageButton = view.findViewById(R.id.btnQuitarDeGenero)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibroVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return LibroVH(v)
    }

    override fun onBindViewHolder(h: LibroVH, pos: Int) {
        val libro = data[pos]
        h.titulo.text = libro.titulo
        h.autor.text = libro.nombreCompletoAutor ?: ""
        h.sinopsis.text = libro.sinopsis ?: ""

        val imageUrl = when {
            !libro.imagenPortada.isNullOrBlank() && (libro.imagenPortada!!.startsWith("http") || libro.imagenPortada!!.startsWith("/")) -> {
                // Si backend guardó URL o ruta relativa
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

        Glide.with(h.itemView.context)
            .load(model)
            .placeholder(R.drawable.ic_placeholder)
            .into(h.portada)

        // Estrellitas según puntuación si disponible (de 0..5)
        val rating = (libro.puntuacionPromedio ?: 0.0).coerceIn(0.0, 5.0)
        val filled = rating.toInt()
        val half = (rating - filled) >= 0.5
        val stars = StringBuilder().apply {
            repeat(filled) { append('★') }
            if (half) append('★')
            val remaining = 5 - length
            repeat(remaining) { append('☆') }
        }.toString()
        h.estrellas.text = stars

        // Delete mode
        h.btnEliminar.visibility = if (deleteMode) View.VISIBLE else View.GONE
        h.btnEliminar.setOnClickListener {
            onDelete?.invoke(libro)
        }

        h.btnQuitarDeGenero.visibility = if (deleteMode) View.VISIBLE else View.GONE
        h.btnQuitarDeGenero.setOnClickListener {
            onRemoveFromGenero?.invoke(libro)
        }

        h.itemView.setOnClickListener { onClick(libro) }
    }

    override fun getItemCount() = data.size
}
