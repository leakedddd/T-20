package com.example.t_20.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.t_20.model.Product

@Dao
interface ProductDao {

    @Query("SELECT * FROM products")
    fun getAll(): List<Product>

    @Query("SELECT * FROM products WHERE category = :category")
    fun getByCategory(category: String): List<Product>

    @Query("SELECT * FROM products WHERE category = :category AND name LIKE '%' || :query || '%'")
    fun searchByCategory(category: String, query: String): List<Product>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%'")
    fun search(query: String): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getById(id: Int): Product?

    @Query("SELECT DISTINCT category FROM products ORDER BY category")
    fun getCategories(): List<String>

    @Insert
    fun insertAll(products: List<Product>)

    @Query("SELECT COUNT(*) FROM products")
    fun getCount(): Int
}
