package com.example.mobileapp.presentation.ui.libro

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AlertDialog
import android.app.DatePickerDialog
import android.widget.ArrayAdapter as WidgetArrayAdapter
import com.bumptech.glide.Glide
import com.example.mobileapp.R
import com.example.mobileapp.data.remote.RetrofitClient
import com.example.mobileapp.data.remote.SessionStore
import com.example.mobileapp.data.remote.model.LibroDTO
import com.example.mobileapp.data.remote.model.genero.GeneroDTO
import com.example.mobileapp.data.remote.model.genero.GeneroLibroDTO
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Calendar

class AddLibroFragment : Fragment(R.layout.fragment_add_libro) {

    private var selectedImageUri: Uri? = null

    private lateinit var ivPreview: ImageView
    private lateinit var spinnerIdioma: Spinner
    private lateinit var spinnerGenero: Spinner
    private lateinit var etNuevoGenero: EditText

    // Mantén la lista de géneros cargados para mapear selección -> idGenero
    private var generosCargados: List<GeneroDTO> = emptyList()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this).load(it).into(ivPreview)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitulo  = view.findViewById<EditText>(R.id.etTitulo)
        val etAutor   = view.findViewById<EditText>(R.id.etAutor)
        val etSinopsis= view.findViewById<EditText>(R.id.etSinopsis)
        val etEditorial = view.findViewById<EditText>(R.id.etEditorial)
        val etIsbn    = view.findViewById<EditText>(R.id.etIsbn)
        val tilIsbn   = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilIsbn)

        val tilEdicion = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilEdicion)
        val actvEdicion = view.findViewById<AutoCompleteTextView>(R.id.actvEdicion)
        val tilEdicionOtra = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilEdicionOtra)
        val etEdicionOtra = view.findViewById<EditText>(R.id.etEdicionOtra)
        val etFecha   = view.findViewById<EditText>(R.id.etFechaLanzamiento)
        val tilFecha  = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilFecha)
        val etPaginas = view.findViewById<EditText>(R.id.etPaginas)
        val btnSeleccionar = view.findViewById<Button>(R.id.btnSeleccionarImagen)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarLibro)
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        val btnCrearGenero = view.findViewById<Button>(R.id.btnCrearGenero)

        ivPreview = view.findViewById(R.id.ivPreview)
        spinnerIdioma = view.findViewById(R.id.spinnerIdioma)
        spinnerGenero = view.findViewById(R.id.spinnerGenero)
        etNuevoGenero = view.findViewById(R.id.etNuevoGenero)

        // Idiomas desde strings.xml (tú ya tienes el array definido)
        android.widget.ArrayAdapter.createFromResource(
            requireContext(),
            R.array.idiomas_array,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerIdioma.adapter = it
        }

        // Cargar géneros desde API
        lifecycleScope.launch {
            try {
                val sessionId = SessionStore.sessionId ?: ""
                val resp = RetrofitClient.generoApi.findAll(sessionId)
                if (resp.isSuccessful) {
                    generosCargados = resp.body().orEmpty()
                    val nombres = generosCargados.map { g -> g.nombre }
                    val adapter = android.widget.ArrayAdapter(requireContext(),
                        android.R.layout.simple_spinner_item, nombres)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerGenero.adapter = adapter
                } else {
                    Toast.makeText(requireContext(), "No se pudieron cargar géneros", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error géneros: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        btnSeleccionar.setOnClickListener { pickImage.launch("image/*") }

        // Edición dropdown + "Otra…"
        val ediciones = resources.getStringArray(R.array.ediciones_array).toList()
        actvEdicion.setAdapter(WidgetArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ediciones))
        actvEdicion.setOnItemClickListener { _, _, position, _ ->
            val selected = ediciones[position]
            if (selected.contains("Otra")) {
                tilEdicionOtra.visibility = View.VISIBLE
                etEdicionOtra.requestFocus()
            } else {
                tilEdicionOtra.visibility = View.GONE
                etEdicionOtra.setText("")
            }
        }

        // Fecha con DatePicker y formato YYYY-MM-DD
        etFecha.setOnClickListener {
            val cal = Calendar.getInstance()
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val d = cal.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(requireContext(), { _, yy, mm, dd ->
                val month = (mm + 1).toString().padStart(2, '0')
                val day = dd.toString().padStart(2, '0')
                etFecha.setText("$yy-$month-$day")
            }, y, m, d).show()
        }

        // ISBN validación ligera (no bloqueante): aviso si no parece ISBN-13 (13 dígitos ignorando guiones)
        etIsbn.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val raw = etIsbn.text?.toString()?.replace("-", "")?.trim() ?: ""
                tilIsbn.error = if (raw.isNotEmpty() && raw.length != 13) getString(R.string.isbn_helper) else null
            }
        }

        // Si no es EMPRESA, cerrar este fragment inmediatamente (no debería ser visible)
        val roleFromStore = SessionStore.rol
        val roleFromPrefs = requireContext()
            .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("USER_ROLE", null)
        val isEmpresa = "EMPRESA".equals((roleFromStore ?: roleFromPrefs)?.trim(), ignoreCase = true)
        if (!isEmpresa) {
            Toast.makeText(requireContext(), "Solo las EMPRESAS pueden agregar libros", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }

        // Asegurar que controles de creación de género solo estén visibles para EMPRESA
        etNuevoGenero.visibility = if (isEmpresa) View.VISIBLE else View.GONE
        btnCrearGenero.visibility = if (isEmpresa) View.VISIBLE else View.GONE

        // Crear género explícitamente y actualizar el spinner
        btnCrearGenero.setOnClickListener {
            val nuevoGeneroNombre = etNuevoGenero.text.toString().trim()
            if (nuevoGeneroNombre.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresa un nombre de género", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Evitar duplicados por nombre (case-insensitive)
            val existente = generosCargados.firstOrNull { it.nombre.equals(nuevoGeneroNombre, ignoreCase = true) }
            if (existente != null) {
                // Seleccionar el existente
                val pos = generosCargados.indexOf(existente)
                if (pos >= 0) spinnerGenero.setSelection(pos)
                Toast.makeText(requireContext(), "El género ya existe, seleccionado en la lista", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val sessionId = SessionStore.sessionId ?: ""
                    val respCreate = RetrofitClient.generoApi.createGenero(
                        sessionId,
                        GeneroDTO(nombre = nuevoGeneroNombre, descripcion = "")
                    )
                    if (!respCreate.isSuccessful) {
                        val code = respCreate.code()
                        val msg = respCreate.errorBody()?.string() ?: ""
                        Toast.makeText(requireContext(), "Crear género falló ($code): $msg", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    val generoCreado = respCreate.body() ?: run {
                        Toast.makeText(requireContext(), "Respuesta vacía al crear género", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    // Actualizar lista y spinner
                    generosCargados = generosCargados + generoCreado
                    val nombres = generosCargados.map { g -> g.nombre }
                    val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerGenero.adapter = adapter
                    // Seleccionar el recién creado
                    val newPos = generosCargados.indexOfFirst { it.idGenero == generoCreado.idGenero }
                    if (newPos >= 0) spinnerGenero.setSelection(newPos)
                    etNuevoGenero.setText("")
                    Toast.makeText(requireContext(), "Género creado y seleccionado", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Excepción al crear género: ${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Botón guardar habilitado para EMPRESA
        btnGuardar.isEnabled = true
        btnGuardar.alpha = 1f

        btnGuardar.setOnClickListener {
            // Ya filtramos el rol al entrar, no es necesario volver a validarlo aquí

            // Validar campos obligatorios
            if (etTitulo.text.toString().trim().isEmpty()) {
                Toast.makeText(requireContext(), "El título es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (etAutor.text.toString().trim().isEmpty()) {
                Toast.makeText(requireContext(), "El autor es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idioma = spinnerIdioma.selectedItem?.toString() ?: "Español"

            // Confirmación antes de crear
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar")
                .setMessage("¿Deseas crear el libro '" + etTitulo.text.toString() + "'?")
                .setPositiveButton("Crear") { _, _ ->
                    // Deshabilitar botón durante el proceso
                    btnGuardar.isEnabled = false
                    btnGuardar.text = "Guardando..."

                    lifecycleScope.launch {
                        try {
                            val sessionId = SessionStore.sessionId ?: ""

                            // 1) Tomar el género seleccionado del spinner (no crear aquí)
                            if (generosCargados.isEmpty()) {
                                Toast.makeText(requireContext(), "No hay géneros para asignar", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            val pos = spinnerGenero.selectedItemPosition.takeIf { it >= 0 } ?: 0
                            val idSel = generosCargados[pos].idGenero
                            val generoId: Long = idSel ?: run {
                                Toast.makeText(requireContext(), "Género inválido", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            // 2) Crear libro
                            val edicionStr = run {
                                val selected = actvEdicion.text?.toString()?.trim()
                                if (selected.isNullOrEmpty()) "" else if (selected.contains("Otra")) {
                                    etEdicionOtra.text?.toString()?.trim().orEmpty()
                                } else selected
                            }

                            val libro = LibroDTO(
                                idLibro = null,
                                titulo = etTitulo.text.toString(),
                                puntuacionPromedio = null,
                                sinopsis = etSinopsis.text.toString(),
                                fechaLanzamiento = etFecha.text.toString(),
                                isbn = etIsbn.text.toString(),
                                edicion = edicionStr,
                                editorial = etEditorial.text.toString(),
                                idioma = idioma,
                                numPaginas = etPaginas.text.toString().toIntOrNull() ?: 0,
                                nombreCompletoAutor = etAutor.text.toString(),
                                imagenPortada = ""
                            )

                            val respLibro = RetrofitClient.libroApi.createLibro(sessionId, libro)

                            if (!respLibro.isSuccessful) {
                                val code = respLibro.code()
                                val errorBody = respLibro.errorBody()?.string()
                                Log.e("CrearLibro", "Error al crear libro ($code): $errorBody")

                                // Try to parse error as JSON
                                try {
                                    val gson = com.google.gson.Gson()
                                    val errorResponse = gson.fromJson(errorBody, com.example.mobileapp.data.remote.model.ApiResponse::class.java)
                                    Toast.makeText(requireContext(), errorResponse.message ?: "Error desconocido", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(requireContext(), "Error crear libro ($code): $errorBody", Toast.LENGTH_LONG).show()
                                }
                                return@launch
                            }

                            val apiResponse = respLibro.body()
                            if (apiResponse?.success == true && apiResponse.data?.idLibro != null) {
                                val libroId = apiResponse.data.idLibro!!

                                // 3) Asignar género al Libro
                                val respRelacion = RetrofitClient.generoLibroApi.create(
                                    sessionId,
                                    com.example.mobileapp.data.remote.model.genero.GeneroLibroDTO(
                                        idGenero = generoId,
                                        idLibro = libroId
                                    )
                                )
                                if (!respRelacion.isSuccessful) {
                                    val code = respRelacion.code()
                                    val msg = respRelacion.errorBody()?.string() ?: ""
                                    Log.e("CrearRelacion", "Error al asignar género al Libro ($code): $msg")
                                    Toast.makeText(requireContext(), "Libro creado, pero falló asignar género ($code): $msg", Toast.LENGTH_LONG).show()
                                }

                                // 4) Subir imagen (opcional)
                                selectedImageUri?.let { subirImagen(sessionId, libroId, it) }

                                Toast.makeText(requireContext(), apiResponse.message ?: "¡Listo! Libro guardado", Toast.LENGTH_SHORT).show()
                                // Volver al listado
                                parentFragmentManager.popBackStack()
                            } else {
                                Toast.makeText(requireContext(), apiResponse?.message ?: "Error en la respuesta del servidor", Toast.LENGTH_LONG).show()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            // Rehabilitar botón
                            btnGuardar.isEnabled = true
                            btnGuardar.text = "Guardar Libro"
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private suspend fun subirImagen(sessionId: String, libroId: Long, uri: Uri) {
        val file = File(requireContext().cacheDir, "tmp_portada.jpg")
        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val resp = RetrofitClient.libroApi.uploadImagen(sessionId, libroId, body)
        if (!resp.isSuccessful) {
            Toast.makeText(requireContext(), "Imagen: error al subir", Toast.LENGTH_SHORT).show()
        }
    }
}
