package com.you4me.you4me

import androidx.room.Database
import androidx.room.RoomDatabase
import com.you4me.you4me.models.User


@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun UserDAO() : UserDAO
}