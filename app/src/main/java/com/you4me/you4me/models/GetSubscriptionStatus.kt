package com.you4me.you4me.models

data class GetSubscriptionStatus(
    val isFreeTrial: Boolean,
    val isPremium: Boolean
)