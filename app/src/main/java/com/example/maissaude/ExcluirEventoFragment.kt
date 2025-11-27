package com.example.maissaude

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ExcluirEventoFragment : Fragment() {

    private lateinit var db: DB
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExcluirEventoAdapter
    private var isAdmin: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_excluir_evento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DB(requireContext())

        recyclerView = view.findViewById(R.id.recyclerExcluirEventos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Recupera tipo do usuário logado
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val tipoUsuario = prefs.getString("tipo_usuario", "usuario") ?: "usuario"
        isAdmin = tipoUsuario == "admin"

        // Carrega eventos do banco
        carregarEventos()
    }

    private fun carregarEventos() {
        val eventos = db.listarEventos().toMutableList()
        Log.d("ExcluirEvento", "Eventos carregados: ${eventos.size}") // Para depuração

        adapter = ExcluirEventoAdapter(eventos, db, isAdmin) {
            // Atualiza lista após exclusão
            atualizarLista()
        }

        recyclerView.adapter = adapter
    }

    private fun atualizarLista() {
        val novosEventos = db.listarEventos().toMutableList()
        adapter.updateEventos(novosEventos)
    }
}
