package com.you4me.you4me.core

import com.you4me.you4me.model.User

class DbRepository(private val appDb: AppDatabase) {
    suspend fun insertUser(user: User) = appDb.UserDAO().saveUserData(user)

//    suspend fun getUser() = appDb.UserDAO().getUser().first()
    suspend fun getUser(): User? {
        val users = appDb.UserDAO().getUser()
        return users.firstOrNull() // Returns null if the list is empty
    }

    suspend fun clear() = appDb.UserDAO().deleteAllUsers()
}
