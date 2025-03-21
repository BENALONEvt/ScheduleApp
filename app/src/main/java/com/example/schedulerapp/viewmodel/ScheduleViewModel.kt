package com.example.schedulerapp.viewmodel

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.schedulerapp.data.Day
import com.example.schedulerapp.data.Schedule
import com.example.schedulerapp.data.ScheduleStorage
import com.example.schedulerapp.repository.ScheduleRepository
import com.example.schedulerapp.util.CsvScheduleParser
import com.example.schedulerapp.util.ScheduleParser
import com.example.schedulerapp.widget.ScheduleWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ScheduleViewModel"
    private val repository: ScheduleRepository
    val schedule: LiveData<Schedule>

    private val _currentDay = MutableLiveData<DayOfWeek>()
    val currentDay: LiveData<DayOfWeek> = _currentDay

    init {
        val scheduleStorage = ScheduleStorage.getInstance(application)
        repository = ScheduleRepository(scheduleStorage)
        schedule = repository.schedule
        
        // Set default current day
        _currentDay.value = DayOfWeek.of(
            try {
                java.time.LocalDate.now().dayOfWeek.value
            } catch (e: Exception) {
                1 // Default to Monday if there's an error
            }
        )
    }

    fun loadSchedule(newSchedule: Schedule) {
        viewModelScope.launch {
            try {
                repository.insertSchedule(newSchedule)
                
                // Update widgets after schedule is saved
                withContext(Dispatchers.Main) {
                    updateWidgets()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading schedule: ${e.message}", e)
            }
        }
    }

    fun setCurrentDay(dayValue: Int) {
        try {
            _currentDay.value = DayOfWeek.of(dayValue.coerceIn(1, 7))
        } catch (e: Exception) {
            Log.e(TAG, "Error setting current day: ${e.message}")
            _currentDay.value = DayOfWeek.MONDAY // Default to Monday
        }
    }

    fun getCurrentDaySchedule(): Day? {
        return try {
            schedule.value?.week?.find { it.name == _currentDay.value }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current day schedule: ${e.message}")
            null
        }
    }

    fun loadScheduleIfNeeded() {
        viewModelScope.launch {
            if (schedule.value == null || schedule.value?.week.isNullOrEmpty()) {
                // Try to load from assets
                try {
                    val application = getApplication<Application>()
                    val inputStream = application.assets.open("sample_schedule.csv")
                    val schedule = CsvScheduleParser.parseCsvInputStream(inputStream)
                    loadSchedule(schedule)
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading sample CSV schedule: ${e.message}")
                    try {
                        val application = getApplication<Application>()
                        val jsonString = application.assets.open("sample_schedule.json").bufferedReader().use { it.readText() }
                        val schedule = ScheduleParser.parseSchedule(jsonString)
                        loadSchedule(schedule)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading sample JSON schedule: ${e.message}")
                        // Create an empty schedule as a last resort
                        loadSchedule(Schedule(week = emptyList()))
                    }
                }
            }
        }
    }
    
    fun updateWidgets() {
        try {
            val context = getApplication<Application>().applicationContext
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ScheduleWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            if (appWidgetIds.isNotEmpty()) {
                // Use ACTION_APPWIDGET_UPDATE for standard widget updates
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                intent.component = componentName
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                context.sendBroadcast(intent)
                
                Log.d(TAG, "Sent broadcast to update ${appWidgetIds.size} widgets from ViewModel")
            } else {
                Log.d(TAG, "No widgets to update from ViewModel")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating widgets from ViewModel: ${e.message}", e)
        }
    }
}

