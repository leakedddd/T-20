package com.example.t_20.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double,
    val originalPrice: Double? = null,
    val imageRes: Int,
    val category: String,
    val stock: Int = 10
)
