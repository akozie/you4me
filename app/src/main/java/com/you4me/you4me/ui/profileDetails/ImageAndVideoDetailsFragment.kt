package com.you4me.you4me.ui.profileDetails

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.you4me.you4me.R

class ImageAndVideoDetailsFragment : Fragment() {
    private val args: ImageAndVideoDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_and_video_details, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val fileUrl = args.IMAGES.fileURL
        val category = args.IMAGES.category

        val frame = view.findViewById<FrameLayout>(R.id.frame)

//        if (category == "image") {
//            val imageView = ImageView(requireContext())
//            imageView.layoutParams =
//                FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.MATCH_PARENT,
//                    FrameLayout.LayoutParams.MATCH_PARENT,
//                )
//            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
//
//            Glide.with(requireContext())
//                .load(fileUrl)
//                .into(imageView)
//            view.findViewById<MaterialButton>(R.id.deleteBtn).text = "Delete Photo"
//            frame.addView(imageView)
//        } else if (category == "video") {
//            view.findViewById<MaterialButton>(R.id.deleteBtn).text = "Delete Video"
//            val videoView = VideoView(requireContext())
//            videoView.layoutParams =
//                FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.MATCH_PARENT,
//                    FrameLayout.LayoutParams.MATCH_PARENT,
//                )
//            videoView.setVideoPath(fileUrl)
//            frame.addView(videoView)
//            videoView.start()
//        }

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
}
