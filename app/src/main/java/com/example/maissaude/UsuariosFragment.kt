package com.example.maissaude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment

class UsuariosFragment : Fragment() {

    private lateinit var dbHelper: DB
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_usuarios, container, false)

        dbHelper = DB(requireContext())
        listView = view.findViewById(R.id.listViewUsuarios)

        // Preencher lista inicialmente
        atualizarLista()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Atualizar lista sempre que voltar ao fragment
        atualizarLista()
    }

    // Função para atualizar a lista
    private fun atualizarLista() {
        val users = dbHelper.getAllUsers().map { user ->
            "${user.first} / ${user.second}"
        }

        if (users.isEmpty()) {
            listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listOf("Nenhum usuário encontrado"))
        } else {
            listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, users)
        }
    }

    // Função de exemplo para inserir usuário e atualizar lista
    fun adicionarUsuario(nome: String, email: String, senha: String) {
        val sucesso = dbHelper.insertUser(nome, email, senha)
        if (sucesso) {
            Toast.makeText(requireContext(), "Usuário adicionado!", Toast.LENGTH_SHORT).show()
            atualizarLista()
        } else {
            Toast.makeText(requireContext(), "Erro ao adicionar usuário!", Toast.LENGTH_SHORT).show()
        }
    }
}


