package com.you4me.you4me.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.you4me.you4me.models.User

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