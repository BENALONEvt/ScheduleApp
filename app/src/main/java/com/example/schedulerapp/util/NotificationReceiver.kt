package com.example.schedulerapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.schedulerapp.MainActivity
import com.example.schedulerapp.R

class NotificationReceiver : BroadcastReceiver() {
    private val TAG = "NotificationReceiver"
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received notification intent")
        
        val lessonName = intent.getStringExtra("lessonName") ?: return
        val startTime = intent.getStringExtra("startTime") ?: return
        val endTime = intent.getStringExtra("endTime")
        val auditorium = intent.getStringExtra("auditorium") ?: return
        val type = intent.getStringExtra("type") ?: "LECTURE"
        val reschedule = intent.getBooleanExtra("reschedule", false)
        
        // Create notification
        showNotification(context, lessonName, startTime, endTime, auditorium, type)
        
        // If this is a one-time notification that needs to be rescheduled for next week,
        // we would handle that here, but we're already using setRepeating in NotificationManager
    }
    
    private fun showNotification(
        context: Context, 
        lessonName: String, 
        startTime: String, 
        endTime: String?, 
        auditorium: String, 
        type: String
    ) {
        val channelId = "schedule_notifications"
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Schedule Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent for when notification is tapped
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val timeInfo = if (endTime != null) "$startTime - $endTime" else startTime
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Скоро начнется занятие")
            .setContentText("$lessonName ($type)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$lessonName ($type)\nВремя: $timeInfo\nАудитория: $auditorium"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Show the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (lessonName + startTime).hashCode()
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        Log.d(TAG, "Showed notification for $lessonName at $startTime")
    }
}

