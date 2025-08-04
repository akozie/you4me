package com.you4me.you4me.models.interests

data class ReceivedInterestResponse(
    val has_next_page: Boolean,
    val interests: List<Interest>,
    val limit: Int,
    val page: Int
)