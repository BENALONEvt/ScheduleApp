package com.example.schedulerapp.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.schedulerapp.R

class ScheduleWidgetConfigActivity : AppCompatActivity() {
    private val TAG = "WidgetConfigActivity"
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    // UI components
    private lateinit var maxLessonsSeekBar: SeekBar
    private lateinit var maxLessonsText: TextView
    private lateinit var currentDayRadio: RadioButton
    private lateinit var nextDayRadio: RadioButton
    private lateinit var showLocationCheckbox: CheckBox
    private lateinit var darkThemeCheckbox: CheckBox
    private lateinit var cancelButton: Button
    private lateinit var applyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the result to CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED)
        
        // Find the widget ID from the intent extras
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        Log.d(TAG, "Widget configuration started for widget ID: $appWidgetId")
        
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e(TAG, "Invalid widget ID received")
            finish()
            return
        }
        
        setContentView(R.layout.activity_widget_config)
        
        // Initialize UI components
        initializeUI()
        
        // Load saved preferences if any
        loadPreferences()
        
        // Set up listeners
        setupListeners()
    }
    
    private fun initializeUI() {
        try {
            maxLessonsSeekBar = findViewById(R.id.seekbar_max_lessons)
            maxLessonsText = findViewById(R.id.text_max_lessons)
            currentDayRadio = findViewById(R.id.radio_current_day)
            nextDayRadio = findViewById(R.id.radio_next_day)
            showLocationCheckbox = findViewById(R.id.checkbox_show_location)
            darkThemeCheckbox = findViewById(R.id.checkbox_dark_theme)
            cancelButton = findViewById(R.id.button_cancel)
            applyButton = findViewById(R.id.button_apply)
            
            // Set initial text for max lessons
            updateMaxLessonsText(maxLessonsSeekBar.progress)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing UI: ${e.message}", e)
            Toast.makeText(this, "Error initializing widget configuration", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun loadPreferences() {
        try {
            val prefs = getSharedPreferences(ScheduleWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE)
            
            // Load preferences with default values if not found
            val maxLessons = prefs.getInt(ScheduleWidgetProvider.getMaxLessonsKey(appWidgetId), 3)
            val showCurrentDay = prefs.getBoolean(ScheduleWidgetProvider.getShowCurrentDayKey(appWidgetId), true)
            val showLocation = prefs.getBoolean(ScheduleWidgetProvider.getShowLocationKey(appWidgetId), true)
            val useDarkTheme = prefs.getBoolean(ScheduleWidgetProvider.getUseDarkThemeKey(appWidgetId), true)
            
            // Apply loaded preferences to UI
            maxLessonsSeekBar.progress = maxLessons
            updateMaxLessonsText(maxLessons)
            currentDayRadio.isChecked = showCurrentDay
            nextDayRadio.isChecked = !showCurrentDay
            showLocationCheckbox.isChecked = showLocation
            darkThemeCheckbox.isChecked = useDarkTheme
        } catch (e: Exception) {
            Log.e(TAG, "Error loading preferences: ${e.message}", e)
            // Continue with default values
        }
    }
    
    private fun setupListeners() {
        try {
            // SeekBar listener
            maxLessonsSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    updateMaxLessonsText(progress)
                }
                
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            
            // Cancel button
            cancelButton.setOnClickListener {
                Log.d(TAG, "Configuration canceled")
                finish()
            }
            
            // Apply button
            applyButton.setOnClickListener {
                savePreferences()
                updateWidget()
                
                // Set result OK with the widget ID
                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(Activity.RESULT_OK, resultValue)
                
                Log.d(TAG, "Configuration applied successfully")
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up listeners: ${e.message}", e)
            Toast.makeText(this, "Error setting up widget configuration", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun updateMaxLessonsText(progress: Int) {
        // Ensure progress is at least 1
        val count = progress.coerceAtLeast(1)
        maxLessonsText.text = "$count ${getCorrectWordForm(count)}"
    }
    
    private fun getCorrectWordForm(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "занятие"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "занятия"
            else -> "занятий"
        }
    }
    
    private fun savePreferences() {
        try {
            val prefs = getSharedPreferences(ScheduleWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            
            // Save all preferences
            editor.putInt(ScheduleWidgetProvider.getMaxLessonsKey(appWidgetId), maxLessonsSeekBar.progress.coerceAtLeast(1))
            editor.putBoolean(ScheduleWidgetProvider.getShowCurrentDayKey(appWidgetId), currentDayRadio.isChecked)
            editor.putBoolean(ScheduleWidgetProvider.getShowLocationKey(appWidgetId), showLocationCheckbox.isChecked)
            editor.putBoolean(ScheduleWidgetProvider.getUseDarkThemeKey(appWidgetId), darkThemeCheckbox.isChecked)
            
            editor.apply()
            Log.d(TAG, "Preferences saved for widget ID: $appWidgetId")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving preferences: ${e.message}", e)
            Toast.makeText(this, "Error saving widget preferences", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateWidget() {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(this)
            ScheduleWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)
            Log.d(TAG, "Widget updated with new configuration")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating widget: ${e.message}", e)
            Toast.makeText(this, "Error updating widget", Toast.LENGTH_SHORT).show()
        }
    }
}

