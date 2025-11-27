package com.example.maissaude

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ExcluirEventoAdapter(
    private val eventos: MutableList<Evento>,
    private val db: DB,
    private val isAdmin: Boolean,
    private val onEventoExcluido: () -> Unit
) : RecyclerView.Adapter<ExcluirEventoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNomeEvento: TextView = itemView.findViewById(R.id.textNomeEvento)
        val textDescricaoEvento: TextView = itemView.findViewById(R.id.textDescricaoEvento)
        val textDataEvento: TextView = itemView.findViewById(R.id.textDataEvento)
        val textHoraEvento: TextView = itemView.findViewById(R.id.textHoraEvento)
        val textLocalEvento: TextView = itemView.findViewById(R.id.textLocalEvento)
        val textTipoEvento: TextView = itemView.findViewById(R.id.textTipoEvento)
        val buttonExcluir: Button = itemView.findViewById(R.id.buttonExcluirEvento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_excluir_evento, parent, false) // Certifique-se de usar o item correto
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val evento = eventos[position]

        holder.textNomeEvento.text = evento.nome
        holder.textDescricaoEvento.text = evento.descricao
        holder.textDataEvento.text = "📅 ${evento.data}"
        holder.textHoraEvento.text = "⏰ ${evento.hora}"
        holder.textLocalEvento.text = "📍 ${evento.local}"
        holder.textTipoEvento.text = "🎯 ${evento.tipo}"

        if (isAdmin) {
            holder.buttonExcluir.visibility = View.VISIBLE
            holder.buttonExcluir.setOnClickListener {
                val sucesso = db.deletarEvento(evento.id)
                if (sucesso) {
                    eventos.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(holder.itemView.context, "Evento excluído!", Toast.LENGTH_SHORT).show()
                    onEventoExcluido()
                } else {
                    Toast.makeText(holder.itemView.context, "Erro ao excluir evento.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.buttonExcluir.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = eventos.size

    // Atualiza lista sem criar outro adapter
    fun updateEventos(novosEventos: List<Evento>) {
        eventos.clear()
        eventos.addAll(novosEventos)
        notifyDataSetChanged()
    }
}
