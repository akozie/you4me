package com.you4me.you4me.ui.main.termsandcondition

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentPrivacyPolicyBinding
import com.you4me.you4me.databinding.FragmentTermsAndConditionBinding
import com.you4me.you4me.utils.Utils
import com.you4me.you4me.utils.savePdf


class TermsAndConditionFragment : Fragment() {

    private lateinit var binding: FragmentTermsAndConditionBinding
    private lateinit var btnDownload: Button
    private lateinit var privacyContent: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTermsAndConditionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnDownload = binding.btnDownloadTerms
        privacyContent = binding.termsContent

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
                    Utils.STORAGE_CODE
                )
            } else {
                savePdf(privacyContent, requireActivity(), requireContext(), "You4meTermsOfAgreement")
            }
        }
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == Utils.STORAGE_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            savePdf(privacyContent, requireActivity(), requireContext(), "You4meTermsOfAgreement")
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}