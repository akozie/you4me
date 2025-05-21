package com.you4me.you4me.ui.profileDetails

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.you4me.you4me.R
import com.you4me.you4me.database.AppDatabase
import com.you4me.you4me.databinding.ActivityImageAndVideoDetailsBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.RemoteDataSource
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.ProfileRepository
import com.you4me.you4me.ui.profile.ProfileViewModel
import com.you4me.you4me.utils.removeSimpleProgressDialog
import com.you4me.you4me.utils.showSimpleProgressDialog

class ImageAndVideoDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageAndVideoDetailsBinding
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageAndVideoDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = ProfileRepository(
            RemoteDataSource().buildApi(
                ApiCollector::class.java
            )
        )
        viewModel = ProfileViewModel(
            repository, DbRepository(
                AppDatabase.invoke(this)
            )
        )

        // Set up the Toolbar using View Binding
        setSupportActionBar(binding.toolbar)

        // Enable the up button (back arrow)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            // Set the title
            title = "" // Replace with your desired title
        }

        // Handle back button click
        binding.toolbar.setNavigationOnClickListener {
            finish() // Navigates back
        }

        val fileUrl = intent.getStringExtra("FILE_URL")
        val category = intent.getStringExtra("CATEGORY")
        val videoId = intent.getStringExtra("VIDEO_ID")
        val userId = intent.getStringExtra("USER_ID")

        val frame = findViewById<FrameLayout>(R.id.frame)
        showLoader(false)

        binding.deleteBtn.setOnClickListener {
            showLoader(true)
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setMessage("Do you want to delete this $category?")
            alertDialog.setNegativeButton("Cancel") { dialog, _ ->
                showLoader(false)
                dialog.dismiss()
            }
            alertDialog.setPositiveButton("Delete") { _, _ ->
                viewModel.deleteVideoUpload(videoId ?: "")
                observeDeleteVideo(userId ?: "")
            }
            alertDialog.setCancelable(false)
            alertDialog.show()
        }

        if (category == "image") {
            val imageView = ImageView(this)
            imageView.layoutParams =
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

            Glide.with(this)
                .load(fileUrl)
                .into(imageView)

            frame.addView(imageView)
        } else if (category == "video") {
            val videoView = VideoView(this)
            videoView.layoutParams =
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            videoView.setVideoPath(fileUrl)
            frame.addView(videoView)

            // Create an overlay button for replay
            val replayButton = ImageView(this)
            replayButton.setImageResource(R.drawable.ic_replay) // Replace with your replay icon
            replayButton.layoutParams =
                FrameLayout.LayoutParams(
                    100,
                    100,
                    Gravity.CENTER
                )
            replayButton.visibility = View.GONE // Initially hidden
            frame.addView(replayButton)

            // Start the video
            videoView.setOnPreparedListener {
                videoView.start()
            }

            // Show replay button when the video ends
            videoView.setOnCompletionListener {
                replayButton.visibility = View.VISIBLE
            }

            // Replay the video when the button is clicked
            replayButton.setOnClickListener {
                videoView.seekTo(0) // Restart from the beginning
                videoView.start()
                replayButton.visibility = View.GONE // Hide button during playback
            }

            // Optionally, allow tap-to-replay
            frame.setOnClickListener {
                if (!videoView.isPlaying) {
                    videoView.seekTo(0)
                    videoView.start()
                    replayButton.visibility = View.GONE
                }
            }
        }
    }

    private fun observeDeleteVideo(userId: String) {
        viewModel.deleteVideoUploadResponse.observe(this) {
            when (it) {
                is Resource.Success -> {
                    showLoader(false)
                    showToast("Deleted Successfully")
                    viewModel.getImagesAndVideos(userId)
                    // Send broadcast to update ProfileFragment
                    val intent = Intent("IMAGE_DELETED")
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                    finish() // Close activity after deletion
                }

                is Resource.Failure -> {
                    showLoader(false)
                }
            }
        }
    }

    private fun showLoader(show: Boolean) {
        if (show) showSimpleProgressDialog() else removeSimpleProgressDialog()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
