package com.example.maissaude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class CadastroFragment : Fragment() {

    private lateinit var dbHelper: DB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cadastro, container, false)
        dbHelper = DB(requireContext())

        val editNome = view.findViewById<EditText>(R.id.editNome)
        val editEmail = view.findViewById<EditText>(R.id.editEmail)
        val editSenha = view.findViewById<EditText>(R.id.editSenha)
        val btnCadastrar = view.findViewById<Button>(R.id.btnCadastrar)

        btnCadastrar.setOnClickListener {
            val nome = editNome.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else {
                val sucesso = dbHelper.insertUser(nome, email, senha)
                if (sucesso) {
                    Toast.makeText(requireContext(), "Usuário cadastrado com sucesso!", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.homeFragment)
                } else {
                    Toast.makeText(requireContext(), "Erro ao cadastrar! E-mail já existente.", Toast.LENGTH_LONG).show()
                }
            }
        }

        return view
    }
}
