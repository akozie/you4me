package com.you4me.you4me.models

data class UpdateUserBody(
    val age_preferred: String,
    val country: String,
    val dob: String,
    val gender: String,
    val name: String,
    val religion: String,
    val religion_preferred: String,
    val sexual_orientation: String,
    val state: String,
    val phone : String
)