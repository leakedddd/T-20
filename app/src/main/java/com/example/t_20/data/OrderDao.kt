package com.example.t_20.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.t_20.model.Order
import com.example.t_20.model.OrderItem
import com.example.t_20.model.OrderWithItems

@Dao
interface OrderDao {

    @Insert
    fun insertOrder(order: Order): Long

    @Insert
    fun insertOrderItems(items: List<OrderItem>)

    @Transaction
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY date DESC")
    fun getOrdersByUser(userId: Int): List<OrderWithItems>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderById(orderId: Int): Order?
}
