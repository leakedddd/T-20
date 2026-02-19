package com.example.t_20.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val date: Long = System.currentTimeMillis(),
    val total: Double,
    val address: String,
    val status: String = "Confirmado"
)
