package com.you4me.you4me

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener

class BillingClientWrapper(context: Context) : PurchasesUpdatedListener {
    private val billingClient = BillingClient
        .newBuilder(context)
        .enablePendingPurchases()
        .setListener(this)
        .build()
    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
        //new purchases callback
    }
}