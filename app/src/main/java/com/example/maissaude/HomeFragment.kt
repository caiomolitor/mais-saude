package com.example.maissaude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val tipoUsuario = prefs.getString("tipo_usuario", "usuario")

        // Botão Usuários (apenas admin)
        val btnUsuarios = view.findViewById<Button>(R.id.btnUsuarios)
        btnUsuarios.visibility = if (tipoUsuario == "admin") View.VISIBLE else View.GONE
        btnUsuarios.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_usuariosFragment)
        }

        // Botão listar Agendamento (apenas admin)
        val btnAgendamentos = view.findViewById<Button>(R.id.btnListarAgendamentos)
        btnAgendamentos.visibility = if (tipoUsuario == "admin") View.VISIBLE else View.GONE
        btnAgendamentos.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_listarAgendamentosFragment)
        }

        // Botão Adicionar eventos (apenas admin)
        val btnEventos = view.findViewById<Button>(R.id.btnEventos)
        btnEventos.visibility = if (tipoUsuario == "admin") View.VISIBLE else View.GONE
        btnEventos.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_adicionarEventoFragment)
        }
        val btnExcluirEvento = view.findViewById<Button>(R.id.btnExcluirEvento)
        btnExcluirEvento.visibility = if (tipoUsuario == "admin") View.VISIBLE else View.GONE
        btnExcluirEvento.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_excluirEventoFragment)
        }

        // Botão Quem Somos
        val btnQuemSomos = view.findViewById<Button>(R.id.btnQuemSomos)
        btnQuemSomos.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_quemSomosFragment)
        }

        // Botão Comunidade Bem Informada
        val btnComunidade = view.findViewById<Button>(R.id.btnComunidade)
        btnComunidade.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_comunidadeFragment)
        }

        // Botão Agendamento
        val btnAgendamento = view.findViewById<Button>(R.id.btnAgendamento)
        btnAgendamento.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_agendamentoFragment)
        }

        // Botão Sair
        val btnSair = view.findViewById<Button>(R.id.btnSair)
        btnSair.setOnClickListener {
            prefs.edit().clear().apply()
            findNavController().navigate(R.id.loginFragment)
        }
    }
}
