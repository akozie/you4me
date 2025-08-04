package com.you4me.you4me.models

data class CompletedDateResponse (
    val has_next_page: Boolean,
    val dates: List<CompletedDateResponseItem>,
    val limit: Int,
    val page: Int
)