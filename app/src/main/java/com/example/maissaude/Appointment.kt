package com.example.maissaude

data class Appointment(
    val date: String,
    val time: String,
    val usuario: String,
    val profissional: String,
    val especializacao: String?,
    val observacoes: String?
)
