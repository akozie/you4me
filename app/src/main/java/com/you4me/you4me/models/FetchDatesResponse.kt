package com.you4me.you4me.models

import com.you4me.you4me.models.interests.Interest

//class FetchDatesResponse: ArrayList<FetchDatesResponseItem>()

data class FetchDatesResponse(
    val has_next_page: Boolean,
    val dates: ArrayList<FetchDatesResponseItem>,
    val limit: Int,
    val page: Int
)


//data class FetchDatesResponse(
//    val data : ArrayList<FetchDatesResponseItem>,
//    val page: Int,
//    val limit: Int,
//    val has_next_page: Boolean
//)