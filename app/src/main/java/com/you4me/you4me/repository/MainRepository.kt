package com.you4me.you4me.repository

import com.you4me.you4me.models.AddDateInterestBody
import com.you4me.you4me.models.AddSwipeBody
import com.you4me.you4me.models.SubmitDateBody
import com.you4me.you4me.network.ApiCollector

class MainRepository(private val apiCollector: ApiCollector) : BaseRepository() {
    suspend fun getPaymentModes() = safeApiCall { apiCollector.getPaymentModes() }

    suspend fun submitDate(submitDateBody: SubmitDateBody) = safeApiCall { apiCollector.submitDate(submitDateBody) }

    suspend fun fetchDates(userId : String) = safeApiCall {
        apiCollector.fetchDates(userId)
    }

    suspend fun addDateInterest(addDateInterestBody: AddDateInterestBody) = safeApiCall {
        apiCollector.addDateInterest(addDateInterestBody)
    }

    suspend fun addSwipe(addSwipeBody: AddSwipeBody) = safeApiCall {
        apiCollector.addSwipe(addSwipeBody)
    }
}