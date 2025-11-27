package com.example.maissaude

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment() {

    private lateinit var dbHelper: DB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Inicializa o banco de dados
        dbHelper = DB(requireContext())

        // Referências aos componentes da tela
        val email = view.findViewById<EditText>(R.id.editEmail)
        val senha = view.findViewById<EditText>(R.id.editSenha)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnCadastrar = view.findViewById<Button>(R.id.btnCadastrar)

        // Botão de login
        btnLogin.setOnClickListener {
            val userEmail = email.text.toString().trim()
            val userSenha = senha.text.toString().trim()

            if (userEmail.isEmpty() || userSenha.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else {
                val loginValido = dbHelper.validarLogin(userEmail, userSenha)
                if (loginValido) {
                    Toast.makeText(requireContext(), "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()

                    // Pega o nome e tipo do usuário logado
                    val nomeUsuario = dbHelper.getNomeUsuario(userEmail)
                    val tipoUsuario = dbHelper.getTipoUsuario(userEmail) ?: "usuario"

                    // Salva no SharedPreferences
                    val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    prefs.edit().apply {
                        putString("nomeUsuario", nomeUsuario)
                        putString("tipo_usuario", tipoUsuario)
                        apply()
                    }

                    // Navega para HomeFragment
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    Toast.makeText(requireContext(), "E-mail ou senha incorretos!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Botão que abre a tela de cadastro
        btnCadastrar.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_cadastroFragment)
        }

        return view
    }
}
