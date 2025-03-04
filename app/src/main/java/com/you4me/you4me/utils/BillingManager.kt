package com.you4me.you4me.utils

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*

class BillingManager(private val activity: Activity) : PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient

    init {
        billingClient = BillingClient.newBuilder(activity)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
//                    Log.d("BillingManager", "Billing client setup successful")
                    val params = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)

                    // uses queryPurchasesAsync Kotlin extension function
//                    billingClient.queryPurchasesAsync(params.build(), purchasesResponseListener)

                }
            }

            override fun onBillingServiceDisconnected() {
//                Log.d("BillingManager", "Billing client disconnected, attempting to reconnect...")
                billingClient.startConnection(this)
            }
        })
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        // Handle purchase updates here
    }

    fun launchBillingFlowForPremium() {
        val skuId = "you4me_premium"
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(SkuDetails(skuId))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}
