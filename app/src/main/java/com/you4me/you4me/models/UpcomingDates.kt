package com.you4me.you4me.models

import com.you4me.you4me.models.interests.Interest


data class UpcomingDates (
    val has_next_page: Boolean,
    val dates: List<UpcomingDatesItem>,
    val limit: Int,
    val page: Int
)