package com.example.maissaude

object AppointmentRepository {

    private val appointments = mutableListOf<Appointment>()

    fun addAppointment(appointment: Appointment): Boolean {
        val existe = appointments.any {
            it.date == appointment.date && it.time == appointment.time
        }

        return if (!existe) {
            appointments.add(appointment)
            true
        } else {
            false
        }
    }

    fun getAppointments(date: String): List<Appointment> {
        return appointments.filter { it.date == date }
    }

    fun clearAll() {
        appointments.clear()
    }
}
