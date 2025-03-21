package com.example.schedulerapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.schedulerapp.R

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val lessonName = inputData.getString(KEY_LESSON_NAME) ?: return Result.failure()
        val startTime = inputData.getString(KEY_START_TIME) ?: return Result.failure()
        val auditorium = inputData.getString(KEY_AUDITORIUM) ?: return Result.failure()

        showNotification(lessonName, startTime, auditorium)

        return Result.success()
    }

    private fun showNotification(lessonName: String, startTime: String, auditorium: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Занятие через 5 минут")
            .setContentText("$lessonName в $startTime, аудитория $auditorium")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val KEY_LESSON_NAME = "lesson_name"
        const val KEY_START_TIME = "start_time"
        const val KEY_AUDITORIUM = "auditorium"
        private const val CHANNEL_ID = "schedule_notification_channel"
        private const val CHANNEL_NAME = "Schedule Notifications"
        private const val NOTIFICATION_ID = 1
    }
}

