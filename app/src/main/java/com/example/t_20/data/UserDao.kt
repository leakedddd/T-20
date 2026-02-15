package com.example.t_20.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.t_20.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getByEmail(email: String): User?

    @Insert
    fun register(user: User): Long
}
