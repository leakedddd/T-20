package com.example.t_20.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val orderId: Int,
    val motivo: String,
    val descripcion: String,
    val imagePath: String,
    val fecha: Long = System.currentTimeMillis(),
    val status: String = "Pendiente"
)
