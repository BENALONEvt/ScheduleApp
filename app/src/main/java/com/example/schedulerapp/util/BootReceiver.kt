package com.example.schedulerapp.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.schedulerapp.data.ScheduleStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val TAG = "BootReceiver"
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, restoring notifications")
            
            // Use coroutine to perform database operations
            CoroutineScope(Dispatchers.IO).launch {
                // Get schedule data
                val scheduleStorage = ScheduleStorage.getInstance(context)
                val schedule = scheduleStorage.schedule.value
                
                if (schedule != null) {
                    // Reschedule all notifications
                    schedule.week.forEachIndexed { index, day ->
                        day.lessons.forEach { lesson ->
                            if (lesson.notificationEnabled) {
                                NotificationManager.scheduleNotification(context, lesson, index + 1)
                            }
                        }
                    }
                    
                    // Update widgets
                    val updateWidgetIntent = Intent("com.example.schedulerapp.UPDATE_WIDGET")
                    context.sendBroadcast(updateWidgetIntent)
                }
            }
        }
    }
}

