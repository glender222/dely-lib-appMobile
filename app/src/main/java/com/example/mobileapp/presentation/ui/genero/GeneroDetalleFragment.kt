package com.example.mobileapp.presentation.ui.genero

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.data.remote.RetrofitClient
import com.example.mobileapp.data.remote.SessionStore
import com.example.mobileapp.data.remote.model.LibroDTO
import com.example.mobileapp.presentation.ui.libro.LibroAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import androidx.appcompat.app.AlertDialog

class GeneroDetalleFragment : Fragment(R.layout.fragment_genero_detalle) {

    private var generoId: Long = -1L
    private var generoNombre: String = ""

    private lateinit var rv: RecyclerView
    private lateinit var adapter: LibroAdapter
    private lateinit var editorialContainer: ChipGroup

    private var librosOriginales: List<LibroDTO> = emptyList()
    private var filtroEditorial: String? = null
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            generoId = it.getLong(ARG_ID, -1L)
            generoNombre = it.getString(ARG_NOMBRE, "") ?: ""
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val tvTituloGenero = view.findViewById<TextView>(R.id.tvTituloGenero)
        val etBuscar = view.findViewById<TextInputEditText>(R.id.etBuscarNombre)
        editorialContainer = view.findViewById(R.id.editorialContainer)
        rv = view.findViewById(R.id.rvLibros)
        val btnToggleDelete = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnToggleDeleteMode)
        val btnEliminarGenero = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEliminarGenero)
        val tvInfoReglas = view.findViewById<TextView>(R.id.tvInfoReglas)
        val bottomActions = view.findViewById<View>(R.id.bottomActions)

        tvTituloGenero.text = generoNombre

        val sessionId = SessionStore.sessionId ?: ""
        adapter = LibroAdapter(sessionId, { libro ->
            Toast.makeText(requireContext(), "Click en ${libro.titulo}", Toast.LENGTH_SHORT).show()
        }, onDelete = { libro ->
            val id = libro.idLibro
            if (id == null) {
                Toast.makeText(requireContext(), "Id de libro inválido", Toast.LENGTH_SHORT).show()
                return@LibroAdapter
            }
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar libro")
                .setMessage("Se quitarán TODAS las asociaciones de género de este libro y luego se eliminará. ¿Deseas continuar y eliminar '${libro.titulo}'?")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                        try {
                            val relsResp = RetrofitClient.generoLibroApi.findGenerosByLibroId(sessionId, id)
                            if (!relsResp.isSuccessful) {
                                Toast.makeText(requireContext(), "No se pudo consultar relaciones (código ${relsResp.code()})", Toast.LENGTH_SHORT).show()
                                return@launchWhenStarted
                            }
                            var quedan = relsResp.body().orEmpty().count { it.estado?.equals("ACTIVO", true) == true }
                            Toast.makeText(requireContext(), "Relaciones encontradas: ${quedan}", Toast.LENGTH_SHORT).show()
                            var intentos = 0
                            while (quedan > 0 && intentos < 3) {
                                intentos++
                                val listado = RetrofitClient.generoLibroApi.findGenerosByLibroId(sessionId, id)
                                if (!listado.isSuccessful) {
                                    Toast.makeText(requireContext(), "No se pudo consultar relaciones (código ${listado.code()})", Toast.LENGTH_SHORT).show()
                                    return@launchWhenStarted
                                }
                                val rels = listado.body().orEmpty().filter { it.estado?.equals("ACTIVO", true) == true }
                                if (rels.isEmpty()) break
                                for (rel in rels) {
                                    val relId = rel.idGeneroLibros
                                    if (relId == null || relId <= 0L) continue
                                    val del = RetrofitClient.generoLibroApi.deleteById(sessionId, relId)
                                    if (!del.isSuccessful) {
                                        val err = try { del.errorBody()?.string() } catch (_: Exception) { null }
                                        Toast.makeText(requireContext(), "Falla al quitar relación (código ${del.code()}) ${err ?: ""}", Toast.LENGTH_LONG).show()
                                        return@launchWhenStarted
                                    }
                                }
                                // verificar de nuevo
                                val verifica = RetrofitClient.generoLibroApi.findGenerosByLibroId(sessionId, id)
                                quedan = if (verifica.isSuccessful) verifica.body().orEmpty().count { it.estado?.equals("ACTIVO", true) == true } else -1
                            }
                            // Fallback: si aún quedan relaciones ACTIVAS, intentar con listado global
                            if (quedan > 0) {
                                val allResp = RetrofitClient.generoLibroApi.findAll(sessionId)
                                if (allResp.isSuccessful) {
                                    val todas = allResp.body().orEmpty()
                                        .filter { it.idLibro == id && it.estado?.equals("ACTIVO", true) == true }
                                    for (rel in todas) {
                                        val relId = rel.idGeneroLibros
                                        if (relId == null || relId <= 0L) continue
                                        val del = RetrofitClient.generoLibroApi.deleteById(sessionId, relId)
                                        if (!del.isSuccessful) {
                                            val err = try { del.errorBody()?.string() } catch (_: Exception) { null }
                                            Toast.makeText(requireContext(), "Fallback: falla al quitar relación (código ${del.code()}) ${err ?: ""}", Toast.LENGTH_LONG).show()
                                            return@launchWhenStarted
                                        }
                                    }
                                    // Verificar nuevamente
                                    val verifica = RetrofitClient.generoLibroApi.findGenerosByLibroId(sessionId, id)
                                    quedan = if (verifica.isSuccessful) verifica.body().orEmpty().count { it.estado?.equals("ACTIVO", true) == true } else -1
                                } else {
                                    Toast.makeText(requireContext(), "Fallback: no se pudo listar todas las relaciones (código ${allResp.code()})", Toast.LENGTH_LONG).show()
                                }
                            }
                            Toast.makeText(requireContext(), "Relaciones restantes tras quitar: ${quedan}", Toast.LENGTH_SHORT).show()
                            if (quedan > 0) {
                                Toast.makeText(requireContext(), "Aún quedan ${quedan} relaciones. No se puede eliminar el libro.", Toast.LENGTH_LONG).show()
                                return@launchWhenStarted
                            }
                            // Intentar eliminar el libro
                            val resp = RetrofitClient.libroApi.deleteLibro(sessionId, id)
                            if (resp.isSuccessful) {
                                Toast.makeText(requireContext(), "Libro eliminado", Toast.LENGTH_SHORT).show()
                                cargarLibros(sessionId)
                            } else {
                                Toast.makeText(requireContext(), "No se pudo eliminar (código ${resp.code()}). Verifica asociaciones restantes.", Toast.LENGTH_LONG).show()
                                // Como mínimo ya se quitó de este género; refrescar lista
                                cargarLibros(sessionId)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }, onRemoveFromGenero = { libro ->
            val id = libro.idLibro ?: return@LibroAdapter
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                try {
                    val relsResp = RetrofitClient.generoLibroApi.findGenerosByLibroId(sessionId, id)
                    if (!relsResp.isSuccessful) {
                        Toast.makeText(requireContext(), "No se pudo consultar relaciones (${relsResp.code()})", Toast.LENGTH_SHORT).show()
                        return@launchWhenStarted
                    }
                    val relacion = relsResp.body().orEmpty()
                        .filter { it.estado?.equals("ACTIVO", true) == true }
                        .firstOrNull { it.idGenero == generoId }
                    val relId = relacion?.idGeneroLibros
                    if (relId == null) {
                        Toast.makeText(requireContext(), "No existe relación con este género", Toast.LENGTH_SHORT).show()
                        return@launchWhenStarted
                    }
                    val del = RetrofitClient.generoLibroApi.deleteById(sessionId, relId)
                    if (del.isSuccessful) {
                        Toast.makeText(requireContext(), "Libro removido del género", Toast.LENGTH_SHORT).show()
                        cargarLibros(sessionId)
                    } else {
                        val err = try { del.errorBody()?.string() } catch (_: Exception) { null }
                        Toast.makeText(requireContext(), "No se pudo remover (${del.code()}) ${err ?: ""}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        cargarLibros(sessionId)

        etBuscar.addTextChangedListener { text ->
            searchQuery = text?.toString()?.trim().orEmpty()
            aplicarFiltros()
        }

        btnToggleDelete.setOnClickListener {
            adapter.deleteMode = !adapter.deleteMode
            btnToggleDelete.text = if (adapter.deleteMode) "Salir eliminación" else "Modo eliminación"
            Toast.makeText(requireContext(), if (adapter.deleteMode) "Selecciona un libro para eliminar" else "Modo eliminación desactivado", Toast.LENGTH_SHORT).show()
        }

        btnEliminarGenero.setOnClickListener {
            // Estricta: sólo si no hay libros asociados
            if (librosOriginales.isNotEmpty()) {
                Toast.makeText(requireContext(), "No puedes eliminar este género: tiene libros asociados", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar género")
                .setMessage("¿Seguro que deseas eliminar el género '${'$'}generoNombre'?")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                        try {
                            val resp = RetrofitClient.generoApi.deleteGenero(sessionId, generoId)
                            if (resp.isSuccessful) {
                            } else {
                                Toast.makeText(requireContext(), "Error al eliminar género: ${'$'}{resp.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error: ${'$'}{e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        fun actualizarUIInfo() {
            val n = librosOriginales.size
            tvInfoReglas.text = if (n > 0) {
                "Género '$generoNombre': $n libro(s) asociados. No puedes eliminar el género mientras tenga libros. Usa 'Quitar y eliminar'."
            } else {
                "Género '$generoNombre' sin libros asociados. Puedes eliminarlo directamente."
            }
            btnEliminarGenero.text = "Eliminar género"
            btnEliminarGenero.isEnabled = n == 0
        }
        // Inicial
        actualizarUIInfo()

        // Ocultar acciones si no es EMPRESA
        val roleFromStore = SessionStore.rol
        val roleFromPrefs = requireContext().getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE).getString("USER_ROLE", null)
        val isEmpresa = "EMPRESA".equals((roleFromStore ?: roleFromPrefs)?.trim(), ignoreCase = true)
        bottomActions.visibility = if (isEmpresa) View.VISIBLE else View.GONE
    }

    private fun cargarLibros(sessionId: String) {
        // Carga simple (sin ViewModel para rapidez). Puedes migrar a ViewModel si lo prefieres.
        // Como estamos en UI thread, usamos enqueue de Retrofit o corrutinas + lifecycleScope si existiera.
        // Aquí usaremos Retrofit de forma bloqueante sólo como ejemplo; en producción usa corrutinas.
        // Para mantener consistencia con el proyecto, haremos una pequeña corrutina:
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            try {
                val resp = RetrofitClient.generoApi.findLibrosByGenero(sessionId, generoId)
                if (resp.isSuccessful) {
                    librosOriginales = resp.body().orEmpty()
                    construirChipsEditorial(librosOriginales)
                    aplicarFiltros()
                } else {
                    Toast.makeText(requireContext(), "Error al cargar libros: ${resp.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun construirChipsEditorial(libros: List<LibroDTO>) {
        editorialContainer.removeAllViews()
        editorialContainer.isSingleSelection = true
        editorialContainer.isSelectionRequired = false

        val editoriales = libros.mapNotNull { it.editorial?.trim() }
            .filter { it.isNotEmpty() }
            .distinct().sorted()

        fun buildChip(text: String, isSelected: Boolean): Chip {
            return Chip(requireContext()).apply {
                this.text = text
                isCheckable = true
                isChecked = isSelected
                isClickable = true
                setOnClickListener {
                    if (text.equals("TODAS", ignoreCase = true)) {
                        editorialContainer.clearCheck()
                        filtroEditorial = null
                    } else {
                        filtroEditorial = text
                        // checked state is managed by ChipGroup
                    }
                    aplicarFiltros()
                }
            }
        }

        // Chip Todas
        editorialContainer.addView(buildChip("TODAS", filtroEditorial == null))
        // Chips por editorial
        editoriales.forEach { ed ->
            val selected = filtroEditorial?.equals(ed, ignoreCase = true) == true
            editorialContainer.addView(buildChip(ed, selected))
        }
    }

    private fun aplicarFiltros() {
        val filtrados = librosOriginales.filter { libro ->
            val pasaEditorial = filtroEditorial?.let { ed -> (libro.editorial ?: "").equals(ed, ignoreCase = true) } ?: true
            val pasaNombre = if (searchQuery.isEmpty()) true else (libro.titulo ?: "").contains(searchQuery, ignoreCase = true)
            pasaEditorial && pasaNombre
        }
        adapter.submit(filtrados)
    }

    companion object {
        private const val ARG_ID = "arg_genero_id"
        private const val ARG_NOMBRE = "arg_genero_nombre"
        fun newInstance(idGenero: Long, nombre: String): GeneroDetalleFragment {
            val f = GeneroDetalleFragment()
            f.arguments = Bundle().apply {
                putLong(ARG_ID, idGenero)
                putString(ARG_NOMBRE, nombre)
            }
            return f
        }
    }
}
