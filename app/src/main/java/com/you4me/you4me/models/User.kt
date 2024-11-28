package com.you4me.you4me.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val userId: String,
    @ColumnInfo val agePreferred: String,
    @ColumnInfo val convertedDate: Int,
    @ColumnInfo val country: String,
    @ColumnInfo val dob: String,
    @ColumnInfo val email: String,
    @ColumnInfo val gender: String,
    @ColumnInfo val isVideoBeingReviewed: String,
    @ColumnInfo val name: String,
    @ColumnInfo val bio: String,
    @ColumnInfo val completionPercentage: String,
    @ColumnInfo val password: String,
    @ColumnInfo val phone: String,
    @ColumnInfo val religion: String,
    @ColumnInfo val religionPreferred: String,
    @ColumnInfo val sexualOrientation: String,
    @ColumnInfo val state: String,
    @ColumnInfo val status: String,
    @ColumnInfo val token: String,
    @ColumnInfo val videoStatus: String,
    @ColumnInfo val videoURL: String,
)
