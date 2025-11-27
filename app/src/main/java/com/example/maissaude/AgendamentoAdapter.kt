package com.example.maissaude

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AgendamentoAdapter(
    private val lista: List<Agendamento>,
    private val listenerEditar: (Agendamento) -> Unit,
    private val listenerExcluir: (Agendamento) -> Unit
) : RecyclerView.Adapter<AgendamentoAdapter.AgendamentoViewHolder>() {

    class AgendamentoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInfo: TextView = view.findViewById(R.id.tvAgendamentoInfo)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        val btnExcluir: ImageButton = view.findViewById(R.id.btnExcluir)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgendamentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agendamento, parent, false)
        return AgendamentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AgendamentoViewHolder, position: Int) {
        val agendamento = lista[position]
        holder.tvInfo.text = "👤 ${agendamento.usuario} 📞 ${agendamento.telefone} | 🩺 ${agendamento.profissional} (${agendamento.especializacao})\n📅 ${agendamento.data} às ${agendamento.hora}"

        holder.btnEditar.setOnClickListener { listenerEditar(agendamento) }
        holder.btnExcluir.setOnClickListener { listenerExcluir(agendamento) }
    }

    override fun getItemCount(): Int = lista.size
}
