package com.example.schedulerapp.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.example.schedulerapp.widget.ScheduleWidgetProvider

/**
 * Helper class for debugging widget issues
 */
object WidgetDebugHelper {
    private const val TAG = "WidgetDebugHelper"
    
    /**
     * Log detailed information about all widgets
     */
    fun logWidgetInfo(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ScheduleWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            Log.d(TAG, "=== Widget Debug Information ===")
            Log.d(TAG, "Number of widgets: ${appWidgetIds.size}")
            
            if (appWidgetIds.isEmpty()) {
                Log.d(TAG, "No widgets found for provider: ${componentName.className}")
            } else {
                for (widgetId in appWidgetIds) {
                    Log.d(TAG, "Widget ID: $widgetId")
                    
                    // Get widget options
                    val options = appWidgetManager.getAppWidgetOptions(widgetId)
                    Log.d(TAG, "  Widget options: $options")
                    
                    // Get widget size
                    val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
                    val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
                    val maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0)
                    val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
                    
                    Log.d(TAG, "  Widget size: min($minWidth×$minHeight), max($maxWidth×$maxHeight)")
                    
                    // Get widget preferences
                    val prefs = context.getSharedPreferences(ScheduleWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE)
                    val maxLessons = prefs.getInt(ScheduleWidgetProvider.getMaxLessonsKey(widgetId), -1)
                    val showCurrentDay = prefs.getBoolean(ScheduleWidgetProvider.getShowCurrentDayKey(widgetId), true)
                    val showLocation = prefs.getBoolean(ScheduleWidgetProvider.getShowLocationKey(widgetId), true)
                    val useDarkTheme = prefs.getBoolean(ScheduleWidgetProvider.getUseDarkThemeKey(widgetId), true)
                    
                    Log.d(TAG, "  Widget preferences: maxLessons=$maxLessons, showCurrentDay=$showCurrentDay, showLocation=$showLocation, useDarkTheme=$useDarkTheme")
                }
            }
            
            // Log provider info
            val providerInfo = context.packageManager.getReceiverInfo(componentName, 0)
            Log.d(TAG, "Provider info: $providerInfo")
            Log.d(TAG, "Provider exported: ${providerInfo.exported}")
            
            // Log manifest info
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            Log.d(TAG, "Package info: $packageInfo")
            Log.d(TAG, "Package version: ${packageInfo.versionName} (${packageInfo.versionCode})")
            
            Log.d(TAG, "=== End Widget Debug Information ===")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging widget info: ${e.message}", e)
        }
    }
    
    /**
     * Check if the widget provider is properly registered
     */
    fun isWidgetProviderRegistered(context: Context): Boolean {
        try {
            val componentName = ComponentName(context, ScheduleWidgetProvider::class.java)
            val providerInfo = context.packageManager.getReceiverInfo(componentName, 0)
            return providerInfo != null && providerInfo.exported
        } catch (e: Exception) {
            Log.e(TAG, "Error checking widget provider: ${e.message}", e)
            return false
        }
    }
}

