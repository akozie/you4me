package com.you4me.you4me.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.*
import com.google.common.collect.ImmutableList
import com.google.gson.JsonObject
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentLikesBinding
import com.you4me.you4me.models.*
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment

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

    private lateinit var user: User
    private var isSubscribed: Boolean? = null
    private var hasCheckedBilling = false

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (!purchases.isNullOrEmpty() && purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED) {
            val p = purchases[0]
            val obj = JsonObject()
            obj.addProperty("purchase_token", p.purchaseToken)
            obj.addProperty("order_id", p.orderId)
            obj.addProperty("purchase_time", p.purchaseTime)
            obj.addProperty("product_id", p.products[0])
            obj.addProperty("period", p.quantity)
            obj.addProperty("user_id", user.userId)
            viewModel.registerPayment(obj)
        }
    }

    private val purchasesResponseListener = PurchasesResponseListener { billingResult, purchases ->
        if (purchases.isNotEmpty() && purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED) {
            hasCheckedBilling = true
            //continue
            if (isSubscribed != null && isSubscribed == false) {
                val obj = JsonObject()

                purchases[0].apply {
                    obj.addProperty("purchase_token", this.purchaseToken)
                    obj.addProperty("order_id", this.orderId)
                    obj.addProperty("purchase_time", this.purchaseTime)
                    obj.addProperty("product_id", this.products[0])
                    obj.addProperty("period", this.quantity)
                    obj.addProperty("user_id", user.userId)
                }
                viewModel.registerPayment(obj)
            }
            Log.d("google play purchase", purchases[0].toString())
        } else {
            hasCheckedBilling = true
            billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
                // check billingResult
                // process returned productDetailsList
                println("billing result code ${billingResult.responseCode}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    println(productDetailsList.joinToString(","))
                    productDetails = productDetailsList.first { it.productId == "you4me_premium" }
                    if (isSubscribed != null && isSubscribed == false) showBilling()
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

        binding.mainLyt.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Save the initial touch position
                    binding.mainLyt.setTag(R.id.tag_touch_start_x, event.x)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Calculate the swipe distance
                    val startX = binding.mainLyt.getTag(R.id.tag_touch_start_x) as Float
                    val endX = event.x
                    val swipeDistance = endX - startX

                    // Apply the tilt animation based on the swipe direction
                    if (swipeDistance > 0) {
                        startTiltAnimation(true)
                        if (currentIdx < 0) {
                            //do nothing
                        } else {
                            showLoading(true)
                            val d = dateInterests[currentIdx]
                            viewModel.updateDateInterest(
                                d.interestID,
                                d.dateID,
                                "PENDING_TIME_APPROVAL"
                            )
                        }
                    } else {
                        startSecondTiltAnimation(true)
                        if (currentIdx < 0) {
                            //do nothing
                        } else {
                            val d = dateInterests[currentIdx]
                            showLoading(true)
                            viewModel.rejectDateInterest(
                                d.interestID, d.dateID, "REJECTED"
                            )
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }


//        binding.mainLyt.setOnTouchListener(object : OnSwipeTouchListener(ctx) {
//            override fun onSwipeLeft() {
//                view?.performClick()
//                super.onSwipeLeft()
//                if (currentIdx < 0) return
//                val d = dateInterests[currentIdx]
//                showLoading(true)
//                viewModel.rejectDateInterest(
//                    d.interestID, d.dateID, "REJECTED"
//                )
//            }
//
//
//            override fun onSwipeRight() {
//                view?.performClick()
//                super.onSwipeRight()
//                if (currentIdx < 0) return
//                showLoading(true)
//                val d = dateInterests[currentIdx]
//                viewModel.updateDateInterest(d.interestID, d.dateID, "PENDING_TIME_APPROVAL")
//            }
//        })
    // }


    private fun startTiltAnimation(isRightSwipe: Boolean) {
        val tiltAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.tilt_animation)
        if (isRightSwipe) {
            tiltAnimation.interpolator = AccelerateDecelerateInterpolator()
        } else {
            tiltAnimation.interpolator = DecelerateInterpolator()
        }
        binding.mainLyt.startAnimation(tiltAnimation)
    }

    private fun startSecondTiltAnimation(isRightSwipe: Boolean) {
        val tiltAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.second_tilt_animation)
        if (isRightSwipe) {
            tiltAnimation.interpolator = AccelerateDecelerateInterpolator()
        } else {
            tiltAnimation.interpolator = DecelerateInterpolator()
        }
        binding.mainLyt.startAnimation(tiltAnimation)
    }

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) {
            user = it
            viewModel.getSubscriptionStatus()
        }
        viewModel.getSubscriptionStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.isFreeTrial || it.value.isPremium) {
                        isSubscribed = true
                        setupView()
                    } else {
                        showAlertDialog(
                            requireContext(),
                            "You need to subscribe to access this screen",
                            "OK"
                        ) {
                            findNavController().popBackStack()
                        }
                        isSubscribed = false
                        showEmpty()
                        showLoading(false)
                        if (hasCheckedBilling) showBilling()
                    }
                }

                is Resource.Failure -> {
                    if (it.message == null && it.errorBody == null) showEmpty()
                    else showAlertDialog(
                        requireContext(),
                        it.message ?: it.errorBody ?: "",
                        "OK"
                    ) {}
                }
            }
        }

        viewModel.fetchDateInterests.observe(viewLifecycleOwner) {
            showLoading(false)
            when (it) {
                is Resource.Success -> {
//                    val dummyData = FetchDateInterest().apply {
//                        add(
//                            FetchDateInterestItem(
//                                "Meeting",
//                                "2024-03-05",
//                                "1",
//                                "Team Meeting",
//                                "Conference Room",
//                                "2024-04-05",
//                                "09:00",
//                                "1",
//                                "",
//                                "",
//                                "",
//                                "",
//                                "",
//                                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
//                            )
//                        )
//                        add(
//                            FetchDateInterestItem(
//                                "Birthday",
//                                "2024-04-10",
//                                "2",
//                                "John's Birthday",
//                                "John's House",
//                                "2024-04-10",
//                                "18:30",
//                                "2",
//                                "",
//                                "",
//                                "",
//                                "",
//                                "",
//                                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
//                            )
//                        )
//                        add(
//                            FetchDateInterestItem(
//                                "Appointment",
//                                "2024-04-15",
//                                "3",
//                                "Dentist Appointment",
//                                "Dentist Clinic",
//                                "2024-04-15",
//                                "11:00",
//                                "3",
//                                "",
//                                "",
//                                "",
//                                "",
//                                "",
//                                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
//                            )
//                        )
//                    }
//                    dateInterests = dummyData
//                    setScreen()
                    if (it.value.isEmpty()) showEmpty()
                    else {
                        dateInterests = it.value
                        setScreen()
                    }
                }

                is Resource.Failure -> {
                    if (it.message == null && it.errorBody == null) showEmpty()
                    else showAlertDialog(
                        requireContext(),
                        it.message ?: it.errorBody ?: "",
                        "OK"
                    ) {}

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
                    showAlertDialog(requireContext(), it.message ?: it.errorBody ?: "", "OK") {}
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
                    showAlertDialog(requireContext(), it.message ?: it.errorBody ?: "", "OK") {}
                }
            }
        }

        viewModel._updatePaymentResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showToast("Payment success!")
                    viewModel.getSubscriptionStatus()

                }

                is Resource.Failure -> {
                    showAlertDialog(requireContext(), it.message ?: it.errorBody ?: "", "OK"){}
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
                val mediaItem =
                    MediaItem.fromUri(dateInterests[currentIdx].videoURL.replace("http:", "https:"))
                it.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
                it.playWhenReady = playWhenReady
                it.prepare()
            }
        }
    }

    private fun showEmpty() {
        binding.mainLyt.visibility = View.GONE
        binding.constraintLayout2.visibility = View.VISIBLE
//        binding.emptyLyt.visibility = View.VISIBLE
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
        Log.d("show billing", "show billing")
        val t = productDetails.subscriptionOfferDetails?.get(0)?.offerToken

        Log.d("PRODUCT_DETAILS", "${productDetails}")
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