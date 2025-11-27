package com.example.maissaude

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class TimeSlotAdapter(
    context: Context,
    private val horarios: List<String>,
    private var ocupados: List<String>
) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, horarios) {

    fun updateOccupied(novosOcupados: List<String>) {
        ocupados = novosOcupados
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view =
            convertView ?: LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
        val tv = view.findViewById<TextView>(android.R.id.text1)
        val horario = horarios[position]

        if (ocupados.contains(horario)) {
            tv.text = "$horario — Indisponivel"
            tv.alpha = 0.5f
            tv.isEnabled = false
        } else {
            tv.text = horario
            tv.alpha = 1f
            tv.isEnabled = true
        }

        return view
    }
}
