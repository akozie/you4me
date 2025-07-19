package com.you4me.you4me.ui.main.privacypolicy

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.you4me.you4me.databinding.FragmentPrivacyPolicyBinding
import com.you4me.you4me.utils.Utils.STORAGE_CODE
import com.you4me.you4me.utils.savePdf
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class PrivacyPolicyFragment :
    Fragment() {

    private lateinit var binding: FragmentPrivacyPolicyBinding
    private lateinit var btnDownload: Button
    private lateinit var privacyContent: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrivacyPolicyBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnDownload = binding.btnDownload
        privacyContent = binding.privacyContent

        btnDownload.setOnClickListener {
            // Ask permission on Android 10 and below
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_CODE
                )
            } else {
                savePdf(privacyContent, requireActivity(), requireContext(), "You4mePrivacyPolicy")
            }
        }
    }


    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == STORAGE_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            savePdf(privacyContent, requireActivity(), requireContext(), "You4mePrivacyPolicy")
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
