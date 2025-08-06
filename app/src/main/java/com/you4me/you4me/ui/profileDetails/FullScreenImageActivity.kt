package com.you4me.you4me.ui.profileDetails

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.you4me.you4me.R
import com.you4me.you4me.adapter.FullScreenImagePagerAdapter
import com.you4me.you4me.databinding.ActivityFullscreenImageBinding

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullscreenImageBinding
    private var imageUrls: ArrayList<String> = arrayListOf()
    private var currentPosition = 0
    private var totalImages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFullscreen()
        getIntentExtras()
        setupViews()
        setupViewPager()
    }

    private fun setupFullscreen() {
        // Hide status bar and navigation bar for true fullscreen
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // Keep screen on while viewing images
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun getIntentExtras() {
        val singleImageUrl = intent.getStringExtra("IMAGE_URL")
        currentPosition = intent.getIntExtra("IMAGE_POSITION", 0)
        totalImages = intent.getIntExtra("TOTAL_IMAGES", 1)
        imageUrls = intent.getStringArrayListExtra("ALL_IMAGE_URLS") ?: arrayListOf()

        // If no list provided, create single image list
        if (imageUrls.isEmpty() && singleImageUrl != null) {
            imageUrls.add(singleImageUrl)
        }
    }

    private fun setupViews() {
        // Back button
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // Update counter
        updateImageCounter()

        // Hide counter if only one image
        if (imageUrls.size <= 1) {
            binding.imageCounter.visibility = View.GONE
        }
    }

    private fun setupViewPager() {
        if (imageUrls.size > 1) {
            // Multiple images - use ViewPager2
            binding.singleImageView.visibility = View.GONE
            binding.viewPager.visibility = View.VISIBLE

            val adapter = FullScreenImagePagerAdapter(imageUrls)
            binding.viewPager.adapter = adapter
            binding.viewPager.setCurrentItem(currentPosition, false)

            binding.viewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    currentPosition = position
                    updateImageCounter()
                }
            })
        } else {
            // Single image - use single ImageView
            binding.viewPager.visibility = View.GONE
            binding.singleImageView.visibility = View.VISIBLE

            Glide.with(this)
                .load(imageUrls.firstOrNull())
                .placeholder(R.drawable.find_date_placeholder)
                .error(R.drawable.find_date_placeholder)
                .into(binding.singleImageView)
        }
    }

    private fun updateImageCounter() {
        binding.imageCounter.text = "${currentPosition + 1}/${imageUrls.size}"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Optional: Add custom exit animation
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down)
    }
}