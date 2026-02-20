package com.example.t_20.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.t_20.model.Ticket

@Dao
interface TicketDao {

    @Insert
    fun insert(ticket: Ticket): Long

    @Query("SELECT * FROM tickets ORDER BY fecha DESC")
    fun getAllTickets(): List<Ticket>

    @Query("SELECT * FROM tickets WHERE id = :ticketId")
    fun getTicketById(ticketId: Int): Ticket?
}
