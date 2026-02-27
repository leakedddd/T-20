package com.example.t_20.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

    @Insert
    fun insert(product: Product): Long

    @Update
    fun update(product: Product)

    @Delete
    fun delete(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    fun deleteById(id: Int)

    @Query("UPDATE products SET stock = :stock WHERE id = :id")
    fun updateStock(id: Int, stock: Int)

    @Query("SELECT COUNT(*) FROM products")
    fun getCount(): Int
}
