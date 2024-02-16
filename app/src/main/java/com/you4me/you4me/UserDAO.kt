package com.you4me.you4me

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.you4me.you4me.models.User

@Dao
interface UserDAO {
    @Query("SELECT * FROM User")
    fun getUser(): List<User>

    @Delete
    fun deleteUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveUserData(user: User)
}