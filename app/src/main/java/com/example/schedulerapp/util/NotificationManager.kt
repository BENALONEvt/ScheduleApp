package com.example.schedulerapp.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.schedulerapp.MainActivity
import com.example.schedulerapp.R
import com.example.schedulerapp.data.Lesson
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object NotificationManager {
    private const val TAG = "NotificationManager"
    private const val CHANNEL_ID = "schedule_notifications"
    private const val CHANNEL_NAME = "Schedule Notifications"
    
    fun createNotificationChannel(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
                )
                channel.description = "Notifications for upcoming lessons"
                
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                    as android.app.NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification channel: ${e.message}")
        }
    }

    fun scheduleNotification(context: Context, lesson: Lesson, dayOfWeek: Int) {
        try {
            if (!lesson.notificationEnabled) {
                Log.d(TAG, "Notifications disabled for lesson: ${lesson.name}")
                return
            }
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Create intent for NotificationReceiver
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("lessonName", lesson.name)
                putExtra("startTime", lesson.startTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                putExtra("endTime", lesson.endTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                putExtra("auditorium", lesson.auditorium)
                putExtra("type", lesson.type.name)
            }
            
            // Create unique ID for this lesson's notification
            val notificationId = (lesson.name + lesson.startTime.toString() + dayOfWeek).hashCode()
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Calculate notification time
            val currentDate = LocalDateTime.now()
            var scheduleDateTime = currentDate
                .with(java.time.DayOfWeek.of(dayOfWeek))
                .withHour(lesson.startTime.hour)
                .withMinute(lesson.startTime.minute)
                .minusMinutes(lesson.notificationMinutesBefore.toLong())
            
            // If the calculated time is in the past, schedule for next week
            if (scheduleDateTime.isBefore(currentDate)) {
                scheduleDateTime = scheduleDateTime.plusWeeks(1)
            }
            
            val triggerTimeMillis = scheduleDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            
            // Schedule the alarm to repeat weekly
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
                
                // Schedule the next week's notification as well
                val nextWeekIntent = Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("lessonName", lesson.name)
                    putExtra("startTime", lesson.startTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                    putExtra("endTime", lesson.endTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                    putExtra("auditorium", lesson.auditorium)
                    putExtra("type", lesson.type.name)
                    putExtra("reschedule", true)  // Flag to indicate this should be rescheduled
                }
                
                val nextWeekPendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId + 100000,  // Different ID to avoid conflict
                    nextWeekIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Schedule for next week
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis + AlarmManager.INTERVAL_DAY * 7,
                    AlarmManager.INTERVAL_DAY * 7,
                    nextWeekPendingIntent
                )
                
                Log.d(TAG, "Scheduled notification for ${lesson.name} on day $dayOfWeek at $scheduleDateTime")
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling alarm: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling notification: ${e.message}")
        }
    }

    fun cancelNotification(context: Context, lesson: Lesson) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Cancel for all possible days of the week
            for (dayOfWeek in 1..7) {
                try {
                    val notificationId = (lesson.name + lesson.startTime.toString() + dayOfWeek).hashCode()
                    
                    val intent = Intent(context, NotificationReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        notificationId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.cancel(pendingIntent)
                    
                    // Also cancel the next week's notification
                    val nextWeekIntent = Intent(context, NotificationReceiver::class.java)
                    val nextWeekPendingIntent = PendingIntent.getBroadcast(
                        context,
                        notificationId + 100000,
                        nextWeekIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.cancel(nextWeekPendingIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error canceling notification for day $dayOfWeek: ${e.message}")
                }
            }
            
            Log.d(TAG, "Cancelled notifications for ${lesson.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling notifications: ${e.message}")
        }
    }
}

