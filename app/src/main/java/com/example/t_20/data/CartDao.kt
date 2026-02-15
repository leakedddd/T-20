package com.example.t_20.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.t_20.model.CartItem
import com.example.t_20.model.CartWithProduct

@Dao
interface CartDao {

    @Transaction
    @Query("SELECT * FROM cart_items")
    fun getCartWithProducts(): List<CartWithProduct>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    fun getCartItem(productId: Int): CartItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cartItem: CartItem)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId")
    fun updateQuantity(productId: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    fun remove(productId: Int)

    @Query("DELETE FROM cart_items")
    fun clearCart()

    @Query("SELECT SUM(quantity) FROM cart_items")
    fun getTotalItems(): Int?

    @Query("SELECT SUM(c.quantity * p.price) FROM cart_items c INNER JOIN products p ON c.productId = p.id")
    fun getTotalPrice(): Double?
}
