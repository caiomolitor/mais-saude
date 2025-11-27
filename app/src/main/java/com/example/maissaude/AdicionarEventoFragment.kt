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

class AdicionarEventoFragment : Fragment() {

    private lateinit var db: DB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_adicionar_evento, container, false)

        db = DB(requireContext())

        val nome = view.findViewById<EditText>(R.id.editNome)
        val descricao = view.findViewById<EditText>(R.id.editDescricao)
        val data = view.findViewById<EditText>(R.id.editData)
        val hora = view.findViewById<EditText>(R.id.editHora) // Novo campo para hora
        val local = view.findViewById<EditText>(R.id.editLocal)
        val tipo = view.findViewById<EditText>(R.id.editTipo)
        val btnSalvar = view.findViewById<Button>(R.id.btnSalvarEvento)

        btnSalvar.setOnClickListener {
            val evento = Evento(
                id = 0,
                nome = nome.text.toString(),
                descricao = descricao.text.toString(),
                data = data.text.toString(),
                hora = hora.text.toString(),
                local = local.text.toString(),
                tipo = tipo.text.toString()
            )

            val sucesso = db.inserirEvento(evento)

            if (sucesso) {
                Toast.makeText(requireContext(), "Evento salvo com sucesso!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // volta para Home ou Comunidade
            } else {
                Toast.makeText(requireContext(), "Erro ao salvar evento.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
