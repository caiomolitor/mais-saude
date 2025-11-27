package com.example.maissaude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListarAgendamentosFragment : Fragment() {

    private lateinit var db: DB
    private lateinit var recyclerView: RecyclerView
    private lateinit var listaAgendamentos: List<Agendamento>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_listar_agendamentos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DB(requireContext())
        recyclerView = view.findViewById(R.id.recyclerViewAgendamentos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        carregarAgendamentos()
    }

    private fun carregarAgendamentos() {
        listaAgendamentos = db.listarAgendamentos()
        recyclerView.adapter = AgendamentoAdapter(listaAgendamentos,
            listenerEditar = { mostrarDialogoEdicao(it) },
            listenerExcluir = {
                val sucesso = db.deletarAgendamento(it.id)
                if (sucesso) {
                    Toast.makeText(requireContext(), "Agendamento excluído!", Toast.LENGTH_SHORT).show()
                    carregarAgendamentos()
                } else {
                    Toast.makeText(requireContext(), "Erro ao excluir agendamento!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun mostrarDialogoEdicao(agendamento: Agendamento) {
        val viewDialog = layoutInflater.inflate(R.layout.dialog_editar_agendamento, null)
        val editData = viewDialog.findViewById<EditText>(R.id.editData)
        val editHora = viewDialog.findViewById<EditText>(R.id.editHora)
        val editObs = viewDialog.findViewById<EditText>(R.id.editObs)

        editData.setText(agendamento.data)
        editHora.setText(agendamento.hora)
        editObs.setText(agendamento.observacoes ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Agendamento")
            .setView(viewDialog)
            .setPositiveButton("Salvar") { _, _ ->
                val novaData = editData.text.toString()
                val novaHora = editHora.text.toString()
                val novasObs = editObs.text.toString()

                val sucesso = db.atualizarAgendamento(agendamento.id, novaData, novaHora, novasObs)
                if (sucesso) {
                    Toast.makeText(requireContext(), "Agendamento atualizado!", Toast.LENGTH_SHORT).show()
                    carregarAgendamentos()
                } else {
                    Toast.makeText(requireContext(), "Erro ao atualizar agendamento!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
