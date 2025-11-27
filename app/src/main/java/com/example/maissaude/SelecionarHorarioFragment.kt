package com.example.maissaude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController

class SelecionarHorarioFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var tvData: TextView
    private lateinit var tvEspecializacao: TextView

    private lateinit var dataSelecionada: String
    private var especializacao: String? = null
    private lateinit var nomePaciente: String
    private lateinit var telefonePaciente: String

    private lateinit var db: DB
    private lateinit var horarios: List<String>
    private lateinit var adapter: TimeSlotAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_selecionar_horario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DB(requireContext())

        tvData = view.findViewById(R.id.tvSelecionarHorario)
        tvEspecializacao = view.findViewById(R.id.tvHorariosDisponiveis)
        listView = view.findViewById(R.id.listViewHorarios)

        // Recebe dados do Bundle
        dataSelecionada = arguments?.getString("dataSelecionada") ?: "Data não informada"
        especializacao = arguments?.getString("especializacao")
        nomePaciente = arguments?.getString("nomePaciente") ?: ""
        telefonePaciente = arguments?.getString("telefonePaciente") ?: ""

        tvData.text = "Data: $dataSelecionada"
        tvEspecializacao.text = "Especialização: ${especializacao ?: "-"}"

        // Gera horários de 30 em 30 minutos
        horarios = gerarHorarios(8, 18, 30)

        // --- Obter profissional pelo nome da especialização ---
        val profissionalId = especializacao?.let { db.getProfissionalIdByEspecializacao(it) }

        if (profissionalId == null) {
            Toast.makeText(requireContext(), "Nenhum profissional encontrado para ${especializacao ?: "esta especialização"}", Toast.LENGTH_LONG).show()
            return
        }

        // Buscar apenas horários ocupados do profissional naquela data
        val ocupados = db.getHorariosOcupados(profissionalId, dataSelecionada)

        adapter = TimeSlotAdapter(requireContext(), horarios, ocupados)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val horarioSelecionado = horarios[position]

            if (ocupados.contains(horarioSelecionado)) {
                Toast.makeText(requireContext(), "Horário já ocupado!", Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar agendamento")
                .setMessage("Deseja confirmar o agendamento para $horarioSelecionado?")
                .setPositiveButton("Confirmar") { dialog, _ ->
                    val usuarioId = 1 // Trocar futuramente para ID real do login
                    val sucesso = db.salvarAgendamento(
                        nomePaciente = nomePaciente,
                        usuarioId = usuarioId,
                        profissionalId = profissionalId,
                        data = dataSelecionada,
                        hora = horarioSelecionado,
                        telefone = telefonePaciente,
                        lembrete = false,
                        observacoes = null
                    )

                    if (sucesso) {
                        Toast.makeText(requireContext(), "Agendamento confirmado para $horarioSelecionado", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_selecionarHorarioFragment_to_homeFragment)
                    } else {
                        Toast.makeText(requireContext(), "Erro ao salvar agendamento!", Toast.LENGTH_SHORT).show()
                    }

                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }


    private fun gerarHorarios(inicio: Int, fim: Int, intervaloMin: Int): List<String> {
        val lista = mutableListOf<String>()
        var hora = inicio
        var minuto = 0
        while (hora < fim || (hora == fim && minuto == 0)) {
            lista.add(String.format("%02d:%02d", hora, minuto))
            minuto += intervaloMin
            if (minuto >= 60) {
                minuto = 0
                hora++
            }
        }
        return lista
    }
}
