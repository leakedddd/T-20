package com.example.t_20.model

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val originalPrice: Double? = null,
    val imageRes: Int,
    val category: String
)
