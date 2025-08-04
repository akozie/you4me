package com.you4me.you4me.ui.main.likes

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.*
import com.bumptech.glide.Glide
import com.google.common.collect.ImmutableList
import com.google.gson.JsonObject
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentLikesBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.models.*
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainViewModel
import com.you4me.you4me.utils.BillingManager
import com.you4me.you4me.utils.SharedPrefHelper.Companion.IS_SUBSCRIBED
import com.you4me.you4me.utils.Utils.getCategoryFromString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LikesFragment : BaseFragment<MainViewModel, FragmentLikesBinding, MainRepository>("DATE_INTEREST_RECEIVED") {
    private var dateInterests = FetchDateInterest()

//    private var dateInterests = ArrayList<FetchDateInterestItem>()
    private var currentIdx = -1

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L

    private lateinit var billingClient: BillingClient
    private lateinit var productDetails: ProductDetails
    private lateinit var queryProductDetailsParams: QueryProductDetailsParams
    private lateinit var billingManager: BillingManager
    private lateinit var user: User
    private var isSubscribed: Boolean = false
    private var displayToast: Boolean = false
    private var hasCheckedBilling = false
    private var isUserSubscribed = false
    private var isFreeTrial = false
    private lateinit var obj: JsonObject
    private lateinit var date: FetchDateInterestItem

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (!purchases.isNullOrEmpty() && purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED) {
                val p = purchases[0]
                val obj = JsonObject()
                obj.addProperty("purchase_token", p.purchaseToken)
                obj.addProperty("order_id", p.orderId)
                obj.addProperty("purchase_time", p.purchaseTime)
                obj.addProperty("product_id", p.products[0])
                obj.addProperty("period", p.quantity)
                obj.addProperty("user_id", user.userId)
//            viewModel.registerPayment(obj)
            }
        }

    private val purchasesResponseListener =
        PurchasesResponseListener { billingResult, purchases ->
            if (purchases.isNotEmpty() && purchases[0].purchaseState == Purchase.PurchaseState.PURCHASED) {
                hasCheckedBilling = true
                // continue
                isUserSubscribed = sharedPrefHelper.getBoolean(IS_SUBSCRIBED)
                Log.d("IS_SUB", isUserSubscribed.toString())
                if (!isUserSubscribed) {
                    obj = JsonObject()
                    purchases[0].apply {
                        obj.addProperty("purchase_token", this.purchaseToken)
                        obj.addProperty("order_id", this.orderId)
                        obj.addProperty("purchase_time", "${this.purchaseTime}")
                        obj.addProperty("product_id", this.products[0])
                        obj.addProperty("period", "${this.quantity}")
                        obj.addProperty("user_id", user.userId)
                    }
                    viewModel.registerPayment(obj)
                    displayToast = true
                }
//                Log.d("google_play_purchase", purchases[0].toString())
//                Log.d("google_new_purchase", obj.toString())
//            viewModel.registerPayment(obj)
            } else {
                hasCheckedBilling = true
                billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
                    // check billingResult
                    // process returned productDetailsList
                    println("billing result code ${billingResult.responseCode}")
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        println(productDetailsList.joinToString(","))
//                    if (productDetails.toString().isNotEmpty()){
                        productDetails = productDetailsList.first { it.productId == "you4me_premium" }
                        if (!isUserSubscribed) {
//                            Log.d("FIRST_PID", productDetailsList.first().toString())
                            showBilling()
                        }
//                    } else {
//                        //
                        // }
                    }
                }
            }
        }

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentLikesBinding {
        return FragmentLikesBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
//        binding.loader.show()
        setupObservers()
//        billingManager = BillingManager(requireActivity())
//        isFreeTrial = sharedPrefHelper.getBoolean(IS_FREE_PLAN)
//        if (!isFreeTrial) {
//            setupBilling()
//        }
        mixpanel?.track("Android_Received_Interest_Viewed")
    }

    override fun onResume() {
        super.onResume()
        initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    private fun observeImagesAndVideos(userId: String) {
        viewModel.getImagesAndVideos(userId)
        viewModel.getImagesAndVideos.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    val listOfImagesAndVideos = it.value
                    try {
                        // Your potentially crashing code (e.g., loading images, videos, etc.)
                        if (isAdded() && getActivity() != null) {
                            // Perform operations safely
//                            loadImagesAndVideosInBackground(listOfImagesAndVideos)
                        }
                    } catch (e: Exception) {
                    }
                }

                is Resource.Failure -> {
                }
            }
        }
    }

//    private fun loadImagesAndVideosInBackground(listOfImagesAndVideos: ImagesVideosResponse) {
//        val imageViews = listOf(binding.frame1, binding.frame2, binding.frame3, binding.frame4)
//
//        var isProfilePictureSet = false
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            withContext(Dispatchers.IO) {
//                listOfImagesAndVideos.take(imageViews.size).asReversed().forEachIndexed { index, fileData ->
//                    val frame = imageViews[index]
//
//                    withContext(Dispatchers.Main) {
//                        if (!isAdded) return@withContext
//
//                        if (fileData.fileURL.isEmpty()) {
//                            frame.visibility = View.GONE // Hide empty frames
//                            return@withContext
//                        } else {
//                            frame.visibility = View.VISIBLE // Show frames with content
//                        }
//
//                        frame.removeAllViews() // Clear previous views
//                        frame.isClickable = true
//                        frame.isFocusable = true
//
//                        val secureUrl = fileData.fileURL.replace("http://", "https://")
//
//                        if (getCategoryFromString(fileData.fileURL) == "image") {
//                            val imageView =
//                                ImageView(requireActivity()).apply {
//                                    layoutParams =
//                                        FrameLayout.LayoutParams(
//                                            FrameLayout.LayoutParams.MATCH_PARENT,
//                                            FrameLayout.LayoutParams.MATCH_PARENT,
//                                        )
//                                    scaleType = ImageView.ScaleType.CENTER_CROP
//                                }
//
//                            Glide.with(requireActivity())
//                                .load(secureUrl)
//                                .into(imageView)
//
//                            frame.addView(imageView)
////                            Log.d("IMAGES_RESSS", "Added image: $secureUrl to frame: ${frame.id}")
//
//                            if (!isProfilePictureSet) {
//                                isProfilePictureSet = true
//                                Glide.with(requireActivity())
//                                    .load(secureUrl)
//                                    .into(binding.imageView)
//                                binding.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
//                            }
//                        } else if (getCategoryFromString(fileData.fileURL) == "video") {
//                            val thumbnailView =
//                                ImageView(requireContext()).apply {
//                                    layoutParams =
//                                        FrameLayout.LayoutParams(
//                                            FrameLayout.LayoutParams.MATCH_PARENT,
//                                            FrameLayout.LayoutParams.MATCH_PARENT,
//                                        )
//                                    scaleType = ImageView.ScaleType.CENTER_CROP
//                                }
//
//                            val bitmap = generateVideoThumbnail(secureUrl)
//
//                            if (bitmap != null) {
//                                thumbnailView.setImageBitmap(bitmap)
//                                frame.addView(thumbnailView)
////                                Log.d("IMAGES_RESSS", "Added video thumbnail for: $secureUrl")
//                            } else {
////                                Log.e("IMAGES_RESSS", "Failed to generate thumbnail for: $secureUrl")
//                            }
//                        }
//
//                        frame.setOnClickListener {
//                            if (fileData.fileURL.isEmpty()) return@setOnClickListener
//                            openDetailScreen(secureUrl, getCategoryFromString(fileData.fileURL), fileData.videoId)
//                        }
//                    }
//                }
//                // Hide remaining frames that didn't get used
//                withContext(Dispatchers.Main) {
//                    for (i in listOfImagesAndVideos.size until imageViews.size) {
//                        imageViews[i].visibility = View.GONE
//                    }
//                }
//            }
//        }
//    }

    private fun generateVideoThumbnail(videoUrl: String): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoUrl, HashMap())
            retriever.frameAtTime
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun openDetailScreen(
        fileUrl: String,
        category: String,
        videoId: String,
    ) {
        val imagesVideosResponseItem =
            ImagesVideosResponseItem(
                category,
                fileUrl,
                "",
                "",
                "",
                videoId,
            )
//        val action = Like.actionFindDateFragmentToImageAndVideoDetailsFragment(imagesVideosResponseItem)
//        findNavController().navigate(action)
    }

//    @SuppressLint("ClickableViewAccessibility")
//    private fun setupView() {
//        binding.cardView.visibility = View.VISIBLE
//        binding.mainLytBtn.visibility = View.VISIBLE
//
//        viewModel.fetchDateInterests(user.userId)
//
//        binding.acceptBtn.setOnClickListener {
//            if (currentIdx < 0 || currentIdx >= dateInterests.size) {
//                return@setOnClickListener // Prevents out-of-bounds access
//            }
//            showLoading(true)
//            val d = dateInterests[currentIdx]
//            viewModel.updateDateInterest(d.interestID, d.dateID, "PENDING_TIME_APPROVAL")
//            mixpanel?.track("Android_Received_Request_Like_Date_Button_Clicked")
//        }
//
//        binding.rejectBtn.setOnClickListener {
//            if (currentIdx < 0 || currentIdx >= dateInterests.size) {
//                return@setOnClickListener // Prevents out-of-bounds access
//            }
//            showLoading(true)
//            val d = dateInterests[currentIdx]
//            viewModel.rejectDateInterest(
//                d.interestID,
//                d.dateID,
//                "REJECTED",
//            )
//            mixpanel?.track("Android_Received_Request_Dislike_Date_Button_Clicked")
//        }
//
////        binding.mainLyt.setOnTouchListener { _, event ->
////            when (event.action) {
////                MotionEvent.ACTION_DOWN -> {
////                    // Save the initial touch position
////                    binding.mainLyt.setTag(R.id.tag_touch_start_x, event.x)
////                    true
////                }
////                MotionEvent.ACTION_UP -> {
////                    // Calculate the swipe distance
////                    val startX = binding.mainLyt.getTag(R.id.tag_touch_start_x) as Float
////                    val endX = event.x
////                    val swipeDistance = endX - startX
////
////                    // Apply the tilt animation based on the swipe direction
////                    if (swipeDistance > 0) {
////                        startTiltAnimation(true)
////                        if (currentIdx < 0) {
////                            // do nothing
////                        } else {
////                            showLoading(true)
////                            val d = dateInterests[currentIdx]
////                            viewModel.updateDateInterest(
////                                d.interestID,
////                                d.dateID,
////                                "PENDING_TIME_APPROVAL",
////                            )
////                        }
////                    } else {
////                        startSecondTiltAnimation(true)
////                        if (currentIdx < 0) {
////                            // do nothing
////                        } else {
////                            val d = dateInterests[currentIdx]
////                            showLoading(true)
////                            viewModel.rejectDateInterest(
////                                d.interestID,
////                                d.dateID,
////                                "REJECTED",
////                            )
////                        }
////                    }
////                    true
////                }
////                else -> false
////            }
////        }
//    }

//    private fun startTiltAnimation(isRightSwipe: Boolean) {
//        val tiltAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.tilt_animation)
//        if (isRightSwipe) {
//            tiltAnimation.interpolator = AccelerateDecelerateInterpolator()
//        } else {
//            tiltAnimation.interpolator = DecelerateInterpolator()
//        }
//        binding.mainLyt.startAnimation(tiltAnimation)
//    }
//
//    private fun startSecondTiltAnimation(isRightSwipe: Boolean) {
//        val tiltAnimation =
//            AnimationUtils.loadAnimation(requireContext(), R.anim.second_tilt_animation)
//        if (isRightSwipe) {
//            tiltAnimation.interpolator = AccelerateDecelerateInterpolator()
//        } else {
//            tiltAnimation.interpolator = DecelerateInterpolator()
//        }
//        binding.mainLyt.startAnimation(tiltAnimation)
//    }

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) {
            user = it
            viewModel.getSubscriptionStatus(user.userId)
            viewModel.loadInterestsForList(user.userId)
        }
        viewModel.getSubscriptionStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.isFreeTrial) {
                        isSubscribed = true
//                        setupView()
                    } else {
                        setupBilling()
                        isSubscribed = false
                        showAlertDialog(
                            requireContext(),
                            "You need to subscribe to access this screen",
                            "OK",
                        ) {
//                            findNavController().popBackStack()
                            if (isAdded) {
                                (parentFragment as? DatesInterestFragment)?.findNavController()?.popBackStack()
                            }
                        }
//                        showEmpty()
//                        showLoading(false)
//                        if (!isUserSubscribed) {
// //                            billingManager.launchBillingFlowForPremium()
//                            Log.d("THIS_IS", hasCheckedBilling.toString())
//                            showBilling()
//                        }
                    }
                }

                is Resource.Failure -> {
                    if (it.message == null && it.errorBody == null) {
//                        showEmpty()
                    } else {
                        showAlertDialog(
                            requireContext(),
                            it.message ?: it.errorBody ?: "",
                            "OK",
                        ) {}
                    }
                }
            }
        }

        viewModel.fetchDateInterests.observe(viewLifecycleOwner) {
//            showLoading(false)
            when (it) {
                is Resource.Success -> {
                    if (it.value.isEmpty()) {
//                        showEmpty()
                    } else {
                        dateInterests = it.value
//                        dateInterests =
//                            arrayListOf(
//                                FetchDateInterestItem(
//                                    userID = "a108fc4e-81e5-415b-b1a1-3874711c0be4",
//                                    submittedBy = "1e2b6e50-3be1-48ea-bf22-adfb2a4fa5b3",
//                                    proposedDate = "February 20, 2025",
//                                    proposedTime = "",
//                                    status = "PENDING",
//                                    createdAt = "2025-01-28 20:42:55",
//                                    age = "24",
//                                    name = "Eniola Moses",
//                                    dateID = "b44389e5-2931-43f0-929a-2d5335e92e6f",
//                                    videoURL = "",
//                                    interestID = "eca40530-4a22-46bc-b66b-6687ee33aa3a",
//                                    venue = "Lekki Conservation Center Lekki Conservation Center, Eti-Osa, Lagos Lekki Conservation Center, Eti-Osa, Lagos",
//                                    originalDate = "2025/02/20",
//                                    originalTime = "08:44",
//                                    state = "Lagos",
//                                ),
//                            )
//                        setScreen()
                    }
                }

                is Resource.Failure -> {
//                    binding.constraintLayout2.visibility = View.VISIBLE
//                    binding.mainLyt.visibility = View.GONE
//                    binding.mainLytBtn.visibility = View.GONE
                }
            }
        }

//        viewModel.rejectDateInterest.observe(viewLifecycleOwner) {
//            showLoading(false)
//            when (it) {
//                is Resource.Success -> {
//                    showToast("Success")
//                    if (currentIdx < dateInterests.lastIndex) {
//                        setScreen()
//                    } else {
//                        showToast("No more dates available")
//                        showEmpty()
//                    }
//                }
//
//                is Resource.Failure -> {
//                    showAlertDialog(requireContext(), it.message ?: it.errorBody ?: "", "OK") {}
//                }
//            }
//        }
//
//        viewModel.updateDateInterest.observe(viewLifecycleOwner) {
//            showLoading(false)
//            when (it) {
//                is Resource.Success -> {
//                    showToast("Success")
//                    if (currentIdx < dateInterests.lastIndex) {
//                        setScreen()
//                    } else {
//                        showToast("No more dates available")
//                        showEmpty()
//                    }
//                }
//
//                is Resource.Failure -> {
//                    showAlertDialog(requireContext(), it.message ?: it.errorBody ?: "", "OK") {}
//                }
//            }
//        }

        if (!isUserSubscribed) {
            viewModel._updatePaymentResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is Resource.Success -> {
//                    isSubscribed = true
                        sharedPrefHelper.saveBoolean(IS_SUBSCRIBED, true)
                        if (displayToast) {
                            showToast("Payment success!")
                        } else {
                            //
                        }
                        viewModel.getSubscriptionStatus(user.userId)
                        Log.d("OK_SUB", isSubscribed.toString())
                    }

                    is Resource.Failure -> {
                        showAlertDialog(requireContext(), it.message ?: it.errorBody ?: "", "OK") {}
                    }
                }
            }
        }
    }

//    private fun setScreen() {
//        binding.cardView.visibility = View.VISIBLE
//        binding.mainLytBtn.visibility = View.VISIBLE
//
//        showLoading(false)
//        currentIdx++
////        val date = dateInterests[currentIdx]
//
//        if (currentIdx < 0 || currentIdx >= dateInterests.size) {
//            // Don't use this index. This is out of bounds (borders, limits, whatever).
//        } else {
//            // Yes, you can safely use this index. The index is present in the array.
//            date = dateInterests[currentIdx]
//        }
//        observeImagesAndVideos(date.userID)
//
//        val mediaItem = MediaItem.fromUri(date.videoURL.replace("http:", "https:"))
//        player?.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
//        player?.playWhenReady = playWhenReady
//        player?.prepare()
//
//        binding.userName.text = "${date.name}, ${date.age}"
//        binding.nameGallery.text = "${date.name}'s Gallery"
//        binding.location.text = "${date.venue} Gallery"
//        binding.nameGallery.text = "${date.name}'s Gallery"
//
////        if (date.bio.isNotEmpty()) {
////            binding.bio.visibility = View.GONE
////        } else {
////            binding.bio.visibility = View.VISIBLE
////        }
//    }

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
//        player =
//            ExoPlayer.Builder(ctx).build().also {
//                binding.userVideo.player = it
//                if (currentIdx != -1) {
//                    val mediaItem =
//                        MediaItem.fromUri(dateInterests[currentIdx].videoURL.replace("http:", "https:"))
//                    it.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
//                    it.playWhenReady = playWhenReady
//                    it.prepare()
//                }
//            }
    }

//    private fun showEmpty() {
//        binding.mainLyt.visibility = View.GONE
//        binding.mainLytBtn.visibility = View.GONE
//
//        // Get the current layout parameters
//        val layoutParams = binding.cardView.layoutParams as? ViewGroup.MarginLayoutParams
//
//        // Check if the cast was successful
//        layoutParams?.let {
//            it.bottomMargin = 0
//            binding.cardView.layoutParams = it
//        }
//
//        binding.constraintLayout2.visibility = View.VISIBLE
////        binding.emptyLyt.visibility = View.VISIBLE
//        binding.loader.visibility = View.GONE
//    }
//
//    private fun showLoading(loading: Boolean) {
//        binding.mainLyt.visibility = if (loading) View.GONE else View.VISIBLE
//        binding.mainLytBtn.visibility = if (loading) View.GONE else View.VISIBLE
//        binding.loader.visibility = if (loading) View.VISIBLE else View.GONE
//    }

    private fun setupBilling() {
        billingClient =
            BillingClient.newBuilder(ctx.applicationContext).setListener(purchasesUpdatedListener)
                .enablePendingPurchases().build()

        queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder().setProductList(
                ImmutableList.of(
                    QueryProductDetailsParams.Product.newBuilder().setProductId("you4me_premium")
                        .setProductType(BillingClient.ProductType.SUBS).build(),
                ),
            ).build()

        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // The BillingClient is ready. You can query purchases here.
                        val params =
                            QueryPurchasesParams.newBuilder()
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
            },
        )
    }

    private fun showBilling() {
        Log.d("show billing", "show billing")
        if (!isUserSubscribed) {
            val t = productDetails.subscriptionOfferDetails?.get(0)?.offerToken

            Log.d("PRODUCT_DETAILS", "$productDetails")
            val productDetailsParamsList =
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                        .setProductDetails(productDetails)
                        // For One-time product, "setOfferToken" method shouldn't be called.
                        // For subscriptions, to get an offer token, call ProductDetails.subscriptionOfferDetails()
                        // for a list of offers that are available to the user
                        .setOfferToken(t!!).build(),
                )

            val billingFlowParams =
                BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList)
                    .build()

            // Launch the billing flow
            billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
//        val billingResult = billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
//        Log.d("BILL_RESULT", billingResult.toString())
        } else {
            //
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mixpanel?.mixpanel?.flush()
        mixpanel?.mixpanel?.optOutTracking()
    }
}
