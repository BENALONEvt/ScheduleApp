package com.example.schedulerapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.schedulerapp.util.CsvScheduleParser
import com.example.schedulerapp.viewmodel.ScheduleViewModel

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var viewModel: ScheduleViewModel
        private val TAG = "SettingsFragment"
        
        // File picker launcher for importing schedule
        private val filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                try {
                    // Get file name for display
                    val fileName = getFileName(uri)
                    Log.d(TAG, "Selected file: $fileName")
                    
                    // Parse the schedule
                    val schedule = CsvScheduleParser.parseSchedule(requireContext(), uri)
                    viewModel.loadSchedule(schedule)
                    
                    Toast.makeText(
                        context, 
                        "Расписание успешно загружено из файла $fileName", 
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading file: ${e.message}", e)
                    Toast.makeText(
                        context, 
                        "Ошибка загрузки файла: ${e.message}", 
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        
        // Helper function to get file name from URI
        private fun getFileName(uri: Uri): String {
            var result = "unknown"
            if (uri.scheme == "content") {
                val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            result = it.getString(nameIndex)
                        }
                    }
                }
            }
            if (result == "unknown") {
                result = uri.path?.substringAfterLast('/')?.substringAfterLast('\\') ?: "unknown"
            }
            return result
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            viewModel = ViewModelProvider(requireActivity())[ScheduleViewModel::class.java]

            // Set up import schedule preference
            findPreference<Preference>("import_schedule")?.setOnPreferenceClickListener {
                // Launch file picker with multiple MIME types to support different file types
                filePickerLauncher.launch("*/*")
                true
            }
            
            setupThemePreference()
        }

        private fun setupThemePreference() {
            val themePreference = findPreference<SwitchPreferenceCompat>("dark_theme")
            
            // Set initial state based on current theme
            val currentNightMode = AppCompatDelegate.getDefaultNightMode()
            themePreference?.isChecked = currentNightMode == AppCompatDelegate.MODE_NIGHT_YES
            
            themePreference?.setOnPreferenceChangeListener { _, newValue ->
                val isDarkTheme = newValue as Boolean
                
                // Save preference
                requireContext().getSharedPreferences("schedule_prefs", Activity.MODE_PRIVATE)
                    .edit()
                    .putBoolean("dark_theme", isDarkTheme)
                    .apply()
                
                // Apply theme
                AppCompatDelegate.setDefaultNightMode(
                    if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                
                true
            }
        }
    }
}

