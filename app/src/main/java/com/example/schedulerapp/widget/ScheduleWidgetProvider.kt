package com.example.schedulerapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.schedulerapp.MainActivity
import com.example.schedulerapp.R
import com.example.schedulerapp.data.Schedule
import com.example.schedulerapp.data.ScheduleStorage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Implementation of App Widget functionality.
 * This widget displays the user's schedule.
 */
class ScheduleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets")
        
        // Update each widget
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        // Handle widget size changes
        updateAppWidget(context, appWidgetManager, appWidgetId)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d(TAG, "onDeleted called for ${appWidgetIds.size} widgets")
        
        // Clean up preferences when widgets are deleted
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        for (appWidgetId in appWidgetIds) {
            editor.remove(getMaxLessonsKey(appWidgetId))
            editor.remove(getShowCurrentDayKey(appWidgetId))
            editor.remove(getShowLocationKey(appWidgetId))
            editor.remove(getUseDarkThemeKey(appWidgetId))
        }
        
        editor.apply()
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
        Log.d(TAG, "onEnabled called - first widget instance created")
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is deleted
        Log.d(TAG, "onDisabled called - last widget instance deleted")
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH_WIDGET) {
            Log.d(TAG, "Received refresh action")
            
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // Update specific widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateAppWidget(context, appWidgetManager, appWidgetId)
                Log.d(TAG, "Refreshed widget ID: $appWidgetId")
            } else {
                // Update all widgets
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, ScheduleWidgetProvider::class.java)
                )
                
                for (id in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id)
                }
                Log.d(TAG, "Refreshed all widgets: ${appWidgetIds.size}")
            }
        }
        
        super.onReceive(context, intent)
    }

    companion object {
        private const val TAG = "ScheduleWidgetProvider"
        const val PREFS_NAME = "com.example.schedulerapp.widget.ScheduleWidgetProvider"
        const val ACTION_REFRESH_WIDGET = "com.example.schedulerapp.widget.ACTION_REFRESH_WIDGET"
        
        // Preference key helpers
        fun getMaxLessonsKey(widgetId: Int) = "widget_${widgetId}_max_lessons"
        fun getShowCurrentDayKey(widgetId: Int) = "widget_${widgetId}_show_current_day"
        fun getShowLocationKey(widgetId: Int) = "widget_${widgetId}_show_location"
        fun getUseDarkThemeKey(widgetId: Int) = "widget_${widgetId}_use_dark_theme"
        
        /**
         * Update a single widget instance
         */
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            try {
                Log.d(TAG, "Updating widget ID: $appWidgetId")
                
                // Load widget preferences
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val maxLessons = prefs.getInt(getMaxLessonsKey(appWidgetId), 3)
                val showCurrentDay = prefs.getBoolean(getShowCurrentDayKey(appWidgetId), true)
                val showLocation = prefs.getBoolean(getShowLocationKey(appWidgetId), true)
                val useDarkTheme = prefs.getBoolean(getUseDarkThemeKey(appWidgetId), true)
                
                // Create RemoteViews
                val views = RemoteViews(context.packageName, R.layout.schedule_widget)
                
                // Get schedule data
                val scheduleStorage = ScheduleStorage.getInstance(context)
                val schedule = scheduleStorage.schedule.value
                
                // Determine which day to show
                val today = LocalDate.now()
                val dayToShow = if (showCurrentDay) {
                    today.dayOfWeek
                } else {
                    // Find next day with lessons
                    findNextDayWithLessons(schedule, today.dayOfWeek)
                }
                
                // Get lessons for the selected day
                val currentDay = schedule?.week?.find { it.name == dayToShow }
                
                // Set widget title
                val dayName = dayToShow.getDisplayName(TextStyle.FULL, Locale("ru"))
                views.setTextViewText(R.id.widget_title, "Расписание на $dayName")
                
                // Set up click intent for the whole widget
                val mainActivityIntent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    mainActivityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                
                // Set up refresh button
                val refreshIntent = Intent(context, ScheduleWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH_WIDGET
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)
                
                // Check if we have schedule data and lessons for the day
                if (schedule == null) {
                    // Error state - no schedule data
                    views.setViewVisibility(R.id.widget_lessons_container, View.GONE)
                    views.setViewVisibility(R.id.widget_empty_message, View.GONE)
                    views.setViewVisibility(R.id.widget_error_message, View.VISIBLE)
                    views.setTextViewText(R.id.widget_error_message, "Нет данных расписания")
                } else if (currentDay == null || currentDay.lessons.isEmpty()) {
                    // Empty state - no lessons for the day
                    views.setViewVisibility(R.id.widget_lessons_container, View.GONE)
                    views.setViewVisibility(R.id.widget_empty_message, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_error_message, View.GONE)
                    views.setTextViewText(R.id.widget_empty_message, "Нет занятий на $dayName")
                } else {
                    // Show lessons
                    views.setViewVisibility(R.id.widget_lessons_container, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_empty_message, View.GONE)
                    views.setViewVisibility(R.id.widget_error_message, View.GONE)
                    
                    // Clear existing lessons
                    views.removeAllViews(R.id.widget_lessons_container)
                    
                    // Sort lessons by start time
                    val sortedLessons = currentDay.lessons.sortedBy { it.startTime }
                    
                    // Add lessons (limit to maxLessons)
                    val lessonsToShow = sortedLessons.take(maxLessons)
                    for (lesson in lessonsToShow) {
                        val lessonView = RemoteViews(context.packageName, R.layout.widget_lesson_item)
                        
                        // Set lesson data
                        lessonView.setTextViewText(R.id.lesson_name, lesson.name)
                        
                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                        val timeText = "${lesson.startTime.format(timeFormatter)} - ${lesson.endTime.format(timeFormatter)}"
                        lessonView.setTextViewText(R.id.lesson_time, timeText)
                        
                        // Show or hide location based on preference
                        if (showLocation) {
                            lessonView.setViewVisibility(R.id.lesson_location, View.VISIBLE)
                            lessonView.setTextViewText(R.id.lesson_location, lesson.auditorium)
                        } else {
                            lessonView.setViewVisibility(R.id.lesson_location, View.GONE)
                        }
                        
                        // Add to container
                        views.addView(R.id.widget_lessons_container, lessonView)
                    }
                }
                
                // Update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
                Log.d(TAG, "Widget $appWidgetId updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget: ${e.message}", e)
                
                // Show error state in widget
                try {
                    val views = RemoteViews(context.packageName, R.layout.schedule_widget)
                    views.setViewVisibility(R.id.widget_lessons_container, View.GONE)
                    views.setViewVisibility(R.id.widget_empty_message, View.GONE)
                    views.setViewVisibility(R.id.widget_error_message, View.VISIBLE)
                    views.setTextViewText(R.id.widget_error_message, "Ошибка обновления виджета")
                    
                    // Set up click intent for the whole widget
                    val mainActivityIntent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        mainActivityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                    
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing error state: ${e.message}", e)
                }
            }
        }
        
        /**
         * Find the next day with lessons starting from the given day
         */
        private fun findNextDayWithLessons(
            schedule: Schedule?, 
            startDay: DayOfWeek
        ): DayOfWeek {
            if (schedule == null) return startDay
            
            // Check each day starting from the next day
            var currentDayValue = startDay.value % 7 + 1
            for (i in 0 until 7) {
                val dayOfWeek = DayOfWeek.of(currentDayValue)
                val day = schedule.week.find { it.name == dayOfWeek }
                
                if (day != null && day.lessons.isNotEmpty()) {
                    return dayOfWeek
                }
                
                currentDayValue = currentDayValue % 7 + 1
            }
            
            // If no day with lessons found, return the start day
            return startDay
        }
    }
}

