package com.you4me.you4me.network

sealed class Resource<out T> {
    data class Success<out T>(val value : T) : Resource<T>()
    data class Failure(
        val isNetworkError : Boolean,
        val errorCode : Int?,
        val message : String?,
        val error : String?,
        val errorBody : String?
    ) : Resource<Nothing>()
}