package com.you4me.you4me.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.common.collect.ImmutableList
import com.you4me.you4me.databinding.FragmentLikesBinding
import com.you4me.you4me.models.FetchDateInterest
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.OnSwipeTouchListener

class LikesFragment : BaseFragment<MainViewModel, FragmentLikesBinding, MainRepository>() {

    private var dateInterests = FetchDateInterest()
    private var currentIdx = -1

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L

    private lateinit var billingClient: BillingClient
    private lateinit var productDetails: ProductDetails
    private lateinit var queryProductDetailsParams: QueryProductDetailsParams

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        // To be implemented in a later section.
    }

    private val purchasesResponseListener = PurchasesResponseListener { billingResult, purchases ->
        if (purchases.isNotEmpty() && purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED) {
            //continue
        } else {
            billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
                // check billingResult
                // process returned productDetailsList
                println("billing result code ${billingResult.responseCode}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    println(productDetailsList.joinToString(","))
                    productDetails = productDetailsList[0]
                    showBilling()
                }
            }
        }
    }

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLikesBinding {
        return FragmentLikesBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loader.show()
        setupObservers()
        setupBilling()
    }

    override fun onResume() {
        super.onResume()
        initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView() {
        viewModel.fetchDateInterests()

        binding.acceptBtn.setOnClickListener {
            if (currentIdx < 0) return@setOnClickListener
            showLoading(true)
            val d = dateInterests[currentIdx]
            viewModel.updateDateInterest(d.interestID, d.dateID, "PENDING_TIME_APPROVAL")
        }
        binding.rejectBtn.setOnClickListener {
            if (currentIdx < 0) return@setOnClickListener
            showLoading(true)
            val d = dateInterests[currentIdx]
            viewModel.rejectDateInterest(
                d.interestID, d.dateID, "REJECTED"
            )
        }

        binding.mainLyt.setOnTouchListener(object : OnSwipeTouchListener(ctx) {
            override fun onSwipeLeft() {
                view?.performClick()
                super.onSwipeLeft()
                if (currentIdx < 0) return
                val d = dateInterests[currentIdx]
                showLoading(true)
                viewModel.rejectDateInterest(
                    d.interestID, d.dateID, "REJECTED"
                )
            }


            override fun onSwipeRight() {
                view?.performClick()
                super.onSwipeRight()
                if (currentIdx < 0) return
                showLoading(true)
                val d = dateInterests[currentIdx]
                viewModel.updateDateInterest(d.interestID, d.dateID, "PENDING_TIME_APPROVAL")
            }
        })
    }

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) { viewModel.getSubscriptionStatus() }
        viewModel.getSubscriptionStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.isFreeTrial || it.value.isPremium) {
                        setupView()
                    } else {
                        showDialog("You need to subscribe to access this screen", false)
                    }
                }

                is Resource.Failure -> {
                    showDialog(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.fetchDateInterests.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.isEmpty()) showEmpty()
                    else {
                        dateInterests = it.value
                        setScreen()
                    }
                }

                is Resource.Failure -> {
                    showDialog(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.rejectDateInterest.observe(viewLifecycleOwner) {
            showLoading(false)
            when (it) {
                is Resource.Success -> {
                    showToast("Success")
                    if (currentIdx < dateInterests.lastIndex) setScreen()
                    else {
                        showToast("No more dates available")
                        showEmpty()
                    }
                }

                is Resource.Failure -> {
                    showDialog(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.updateDateInterest.observe(viewLifecycleOwner) {
            showLoading(false)
            when (it) {
                is Resource.Success -> {
                    showToast("Success")
                    if (currentIdx < dateInterests.lastIndex) setScreen()
                    else {
                        showToast("No more dates available")
                        showEmpty()
                    }
                }

                is Resource.Failure -> {
                    showDialog(it.message ?: it.errorBody ?: "")
                }
            }
        }
    }

    private fun setScreen() {
        showLoading(false)
        currentIdx++
        val date = dateInterests[currentIdx]

        val mediaItem = MediaItem.fromUri(date.videoURL.replace("http:", "https:"))
        player?.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
        player?.playWhenReady = playWhenReady
        player?.prepare()

        binding.userName.text = "${date.name}, ${date.age}"
        binding.location.text = date.venue
        binding.dateODate.text = "${date.proposedDate} : ${date.proposedTime}"
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            mediaItemIndex = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(ctx).build().also {
            binding.userVideo.player = it
            if (currentIdx != -1) {
                val mediaItem = MediaItem.fromUri(dateInterests[currentIdx].videoURL.replace("http:", "https:"))
                it.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
                it.playWhenReady = playWhenReady
                it.prepare()
            }
        }
    }

    private fun showEmpty() {
        binding.mainLyt.visibility = View.GONE
        binding.emptyLyt.visibility = View.VISIBLE
        binding.loader.visibility = View.GONE
    }

    private fun showLoading(loading: Boolean) {
        binding.mainLyt.visibility = if (loading) View.GONE else View.VISIBLE
        binding.loader.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun setupBilling() {
        billingClient =
            BillingClient.newBuilder(ctx.applicationContext).setListener(purchasesUpdatedListener)
                .enablePendingPurchases().build()

        queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(
            ImmutableList.of(
                QueryProductDetailsParams.Product.newBuilder().setProductId("you4me_premium")
                    .setProductType(BillingClient.ProductType.SUBS).build()
            )
        ).build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    val params = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)

                    // uses queryPurchasesAsync Kotlin extension function
                    println("called query purchases async")
                    billingClient.queryPurchasesAsync(params.build(), purchasesResponseListener)

                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private fun showBilling() {
        println("show billing")
        val t = productDetails.subscriptionOfferDetails?.get(0)?.offerToken

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(productDetails)
                // For One-time product, "setOfferToken" method shouldn't be called.
                // For subscriptions, to get an offer token, call ProductDetails.subscriptionOfferDetails()
                // for a list of offers that are available to the user
                .setOfferToken(t!!).build()
        )

        val billingFlowParams =
            BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList)
                .build()

// Launch the billing flow
        val billingResult = billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
    }
}