package com.you4me.you4me.ui.main.applanguage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat.recreate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.adapter.languageSelection.LanguageAdapter
import com.you4me.you4me.databinding.FragmentLangugaeSelectionBinding
import com.you4me.you4me.utils.LocaleHelper
import com.you4me.you4me.utils.LocaleHelper.getLanguageCode
import com.you4me.you4me.utils.LocaleHelper.saveLanguageToPreferences

class LanguageSelectionFragment : Fragment() {

    private lateinit var binding: FragmentLangugaeSelectionBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LanguageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLangugaeSelectionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerLanguages
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val languages = getLanguageList()
        adapter = LanguageAdapter(languages) { selectedLanguage ->
            val languageCode = getLanguageCode(selectedLanguage)

            saveLanguageToPreferences(requireContext(), languageCode)
            LocaleHelper.setLocale(requireContext(), languageCode)
            recreate(requireActivity()) // refresh UI in new language

//            val intent = Intent(this, MainActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            finish()

        }


        recyclerView.adapter = adapter
    }

    private fun getLanguageList(): List<String> {
        return listOf(
            "English", "Deutsch", "Français", "Español",
            "العربية", "বাংলা", "中文 (简体)", "中文 (繁體)",
            "中文（繁體, 香港）", "Hrvatski", "Čeština", "Dansk", "Nederlands",
            "English (UK)", "Suomi", "Deutsch (Germany)", "Ελληνικά", "עברית",
            "हिन्दी", "Magyar", "Bahasa Indonesia", "Italiano", "日本語", "한국어",
            "Bahasa Melayu", "Norsk bokmål", "Polski", "Português", "Português (Portugal)",
            "Română", "Русский", "Slovenčina", "Español (España)", "Svenska", "ไทย",
            "Türkçe", "Українська", "Tiếng Việt"
        )
    }
}
