package com.you4me.you4me.ui.profileDetails

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentImageAndVideoDetailsBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.ProfileRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.profile.ProfileViewModel

class ImageAndVideoDetailsFragment : BaseFragment<ProfileViewModel, FragmentImageAndVideoDetailsBinding, ProfileRepository>() {
    //    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?,
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_image_and_video_details, container, false)
//    }
    val args: ImageAndVideoDetailsFragmentArgs by navArgs()

    override fun getViewModel() = ProfileViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentImageAndVideoDetailsBinding {
        return FragmentImageAndVideoDetailsBinding.inflate(layoutInflater)
    }

    override fun getRepository() = ProfileRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val fileUrl = args.IMAGES.fileURL
        val category = args.IMAGES.category
        val videoId = args.IMAGES.videoId

        val frame = view.findViewById<FrameLayout>(R.id.frame)
        showLoader(false)
        binding.deleteBtn.setOnClickListener {
            showLoader(true)
            val alertDialog = AlertDialog.Builder(ctx)
            alertDialog.setMessage("Do you want to delete this $category")
            alertDialog.setNegativeButton("Cancel") { dialog, int ->
                dialog.dismiss()
            }
            alertDialog.setPositiveButton("Delete") { dialog, int ->
                viewModel.deleteVideoUpload(videoId)
                observeDeleteVideo()
            }
            alertDialog.setCancelable(false)
            alertDialog.show()
        }

        if (category == "image") {
            val imageView = ImageView(requireContext())
            imageView.layoutParams =
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP

            Glide.with(requireContext())
                .load(fileUrl)
                .into(imageView)

            frame.addView(imageView)
        } else if (category == "video") {
            val videoView = VideoView(requireContext())
            videoView.layoutParams =
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )
            videoView.setVideoPath(fileUrl)
            frame.addView(videoView)

            // Create an overlay button for replay
            val replayButton = ImageView(requireContext())
            replayButton.setImageResource(R.drawable.ic_replay) // Replace with your replay icon
            replayButton.layoutParams =
                FrameLayout.LayoutParams(
                    100, // Width in pixels or use ViewGroup.LayoutParams.WRAP_CONTENT
                    100, // Height in pixels or use ViewGroup.LayoutParams.WRAP_CONTENT
                    Gravity.CENTER,
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

    private fun observeDeleteVideo() {
        viewModel.deleteVideoUploadResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showLoader(false)
                    showToast("Deleted Successfully")
                    viewModel.getImagesAndVideos(args.IMAGES.userId)
                    findNavController().popBackStack()
                }

                is Resource.Failure -> {
                    showLoader(false)
                }
            }
        }
    }

    private fun showLoader(show: Boolean) {
        binding.progressCircular.visibility = if (show) View.VISIBLE else View.GONE
    }
}
