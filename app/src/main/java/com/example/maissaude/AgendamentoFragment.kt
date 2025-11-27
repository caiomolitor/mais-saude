package com.example.maissaude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.*

class AgendamentoFragment : Fragment() {

    private var dataSelecionada: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agendamento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etUsuario = view.findViewById<EditText>(R.id.etUsuario)
        val etTelefone = view.findViewById<EditText>(R.id.etTelefone)
        val spinnerEspecializacao = view.findViewById<Spinner>(R.id.spinnerEspecializacao)
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)

        // Configura Spinner
        val especializacoes = listOf("Cardiologia", "Dermatologia", "Nutrição", "Fisioterapia")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, especializacoes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEspecializacao.adapter = adapter

        // Define data atual como selecionada inicialmente
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dataSelecionada = sdf.format(Date())

        // Ao selecionar uma data, abre SelecionarHorarioFragment
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val data = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            dataSelecionada = data

            val nomePaciente = etUsuario.text?.toString()?.trim() ?: ""
            val telefonePaciente = etTelefone.text?.toString()?.trim() ?: ""

            if (nomePaciente.isEmpty()) {
                Toast.makeText(requireContext(), "Digite o nome do paciente", Toast.LENGTH_SHORT).show()
                return@setOnDateChangeListener
            }

            if (telefonePaciente.isEmpty()) {
                Toast.makeText(requireContext(), "Digite o telefone do paciente", Toast.LENGTH_SHORT).show()
                return@setOnDateChangeListener
            }

            // Prepara o bundle
            val bundle = Bundle().apply {
                putString("dataSelecionada", dataSelecionada)
                putString("especializacao", spinnerEspecializacao.selectedItem.toString())
                putString("nomePaciente", nomePaciente)
                putString("telefonePaciente", telefonePaciente)
            }

            if (isAdded) {
                try {
                    findNavController().navigate(
                        R.id.action_agendamentoFragment_to_selecionarHorarioFragment,
                        bundle
                    )
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Erro ao abrir a tela de horários", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}