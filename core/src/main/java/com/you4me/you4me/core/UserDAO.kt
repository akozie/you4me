package com.you4me.you4me.core

import androidx.room.*
import com.you4me.you4me.model.User

@Dao
interface UserDAO {
    @Query("SELECT * FROM User")
    suspend fun getUser(): List<User>

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM User")
    suspend fun deleteAllUsers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserData(user: User)
}