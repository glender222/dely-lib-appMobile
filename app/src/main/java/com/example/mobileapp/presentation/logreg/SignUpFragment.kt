package com.example.mobileapp.presentation.logreg

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mobileapp.R
import com.example.mobileapp.data.remote.RetrofitClient
import com.example.mobileapp.data.repository.AuthRepository
import com.google.android.material.textfield.TextInputEditText

class SignUpFragment : Fragment(R.layout.fragment_signup) {

    private val registerViewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory(AuthRepository(RetrofitClient.authApi))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nombreEt = view.findViewById<TextInputEditText>(R.id.etNombre)
        val apellidoEt = view.findViewById<TextInputEditText>(R.id.etApellido)
        val emailEt = view.findViewById<TextInputEditText>(R.id.etCorreo)
        val passEt = view.findViewById<TextInputEditText>(R.id.etPassword)
        val fechaEt = view.findViewById<TextInputEditText>(R.id.etFecha)
        val btnSignUp = view.findViewById<Button>(R.id.btnSignUp)
        val btnBack = view.findViewById<Button>(R.id.btnBack) // 👈 ahora sí

        // Acción del botón volver
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack() // vuelve al fragmento anterior (login)
        }

        // Acción del botón registrarse
        btnSignUp.setOnClickListener {
            val nombre = nombreEt.text.toString().trim()
            val apellido = apellidoEt.text.toString().trim()
            val email = emailEt.text.toString().trim()
            val password = passEt.text.toString().trim()
            val fecha = fechaEt.text.toString().trim() // formato yyyy-MM-dd

            if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerViewModel.register(nombre, apellido, email, password, fecha)
        }

        // Observador del resultado de registro
        registerViewModel.registerResult.observe(viewLifecycleOwner) { response ->
            if (response != null && response.isSuccessful) {
                Toast.makeText(requireContext(), "Registro exitoso!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack() // vuelve al login
            } else {
                val errorMsg = response?.errorBody()?.string() ?: "Error al registrar usuario"
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}