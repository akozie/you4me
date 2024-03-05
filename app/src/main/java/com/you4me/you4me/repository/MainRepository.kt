package com.you4me.you4me.repository

import com.you4me.you4me.models.UpdateDateInterestBody
import com.you4me.you4me.models.AddDateInterestBody
import com.you4me.you4me.models.AddSwipeBody
import com.you4me.you4me.models.ProposeNewDateTimeBody
import com.you4me.you4me.models.RejectDateInterestBody
import com.you4me.you4me.models.SubmitDateBody
import com.you4me.you4me.network.ApiCollector

class MainRepository(private val apiCollector: ApiCollector) : BaseRepository() {
    suspend fun getPaymentModes() = safeApiCall { apiCollector.getPaymentModes() }

    suspend fun submitDate(submitDateBody: SubmitDateBody) =
        safeApiCall { apiCollector.submitDate(submitDateBody) }

    suspend fun fetchDates(userId: String) = safeApiCall {
        apiCollector.fetchDates(userId)
    }

    suspend fun addDateInterest(addDateInterestBody: AddDateInterestBody) = safeApiCall {
        apiCollector.addDateInterest(addDateInterestBody)
    }

    suspend fun addSwipe(addSwipeBody: AddSwipeBody) = safeApiCall {
        apiCollector.addSwipe(addSwipeBody)
    }

    suspend fun fetchDateInterest(userId: String) = safeApiCall {
        apiCollector.fetchDateInterests(userId)
    }

    suspend fun getSubscriptionStatus(userId: String) = safeApiCall {
        apiCollector.getSubscriptionStatus(userId)
    }

    suspend fun rejectDateInterest(interestId: String, rejectDate: RejectDateInterestBody) =
        safeApiCall {
            apiCollector.rejectDateInterest(interestId, rejectDate)
        }

    suspend fun acceptDateInterest(interestId: String, acceptDate: UpdateDateInterestBody) =
        safeApiCall { apiCollector.updateDateInterest(interestId, acceptDate) }

    suspend fun getUpcomingDates(userId: String) =
        safeApiCall { apiCollector.getUpcomingDates(userId) }

    suspend fun inviteeDatesRequiringApproval(userId: String) =
        safeApiCall { apiCollector.getInviteeDatesRequiringApproval(userId) }

    suspend fun getDateInterestsRequiringApproval(userId: String) = safeApiCall {
        apiCollector.getDateInterestsRequiringApproval(userId)
    }

    suspend fun proposeNewDateTime(userId: String, interestId: String, body: ProposeNewDateTimeBody) = safeApiCall {
        apiCollector.proposeNewDateTime(userId, interestId, body)
    }

}