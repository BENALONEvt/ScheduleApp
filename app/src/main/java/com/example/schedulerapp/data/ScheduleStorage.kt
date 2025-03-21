package com.example.schedulerapp.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ScheduleStorage(context: Context) {
    private val TAG = "ScheduleStorage"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson: Gson
    
    private val _schedule = MutableLiveData<Schedule>()
    val schedule: LiveData<Schedule> = _schedule
    
    init {
        // Create a custom Gson instance that can handle LocalTime and DayOfWeek
        gson = GsonBuilder()
            .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
            .registerTypeAdapter(DayOfWeek::class.java, DayOfWeekAdapter())
            .create()
        
        loadSchedule()
    }
    
    private fun loadSchedule() {
        val scheduleJson = prefs.getString(KEY_SCHEDULE, null)
        Log.d(TAG, "Loading schedule from SharedPreferences, JSON: ${scheduleJson?.take(100)}...")
        
        if (scheduleJson != null) {
            try {
                val schedule = gson.fromJson(scheduleJson, Schedule::class.java)
                _schedule.postValue(schedule)
                Log.d(TAG, "Successfully loaded schedule with ${schedule.week.size} days")
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing schedule JSON: ${e.message}", e)
                // If there's an error, create an empty schedule
                _schedule.postValue(Schedule(week = emptyList()))
            }
        } else {
            Log.d(TAG, "No saved schedule found, creating empty schedule")
            // No saved schedule, create an empty one
            _schedule.postValue(Schedule(week = emptyList()))
        }
    }
    
    suspend fun saveSchedule(schedule: Schedule) {
        try {
            val scheduleJson = gson.toJson(schedule)
            Log.d(TAG, "Saving schedule to SharedPreferences, JSON: ${scheduleJson.take(100)}...")
            
            // Use commit() instead of apply() to ensure immediate write
            prefs.edit().putString(KEY_SCHEDULE, scheduleJson).commit()
            _schedule.postValue(schedule)
            Log.d(TAG, "Successfully saved schedule with ${schedule.week.size} days")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving schedule: ${e.message}", e)
        }
    }
    
    companion object {
        const val PREFS_NAME = "schedule_prefs"
        const val KEY_SCHEDULE = "KEY_SCHEDULE"
        
        @Volatile
        private var INSTANCE: ScheduleStorage? = null
        
        fun getInstance(context: Context): ScheduleStorage {
            return INSTANCE ?: synchronized(this) {
                val instance = ScheduleStorage(context)
                INSTANCE = instance
                instance
            }
        }
    }
    
    // Custom adapter for LocalTime serialization/deserialization
    private class LocalTimeAdapter : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
        private val formatter = DateTimeFormatter.ofPattern("HH:mm")
        
        override fun serialize(src: LocalTime, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(formatter.format(src))
        }
        
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalTime {
            return LocalTime.parse(json.asString, formatter)
        }
    }
    
    // Custom adapter for DayOfWeek serialization/deserialization
    private class DayOfWeekAdapter : JsonSerializer<DayOfWeek>, JsonDeserializer<DayOfWeek> {
        override fun serialize(src: DayOfWeek, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.name)
        }
        
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DayOfWeek {
            return DayOfWeek.valueOf(json.asString)
        }
    }
}

