package com.example.schedulerapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.schedulerapp.MainActivity
import com.example.schedulerapp.R
import com.example.schedulerapp.data.ScheduleStorage
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Простейшая реализация виджета расписания
 */
class BasicScheduleWidget : AppWidgetProvider() {
    private val TAG = "BasicScheduleWidget"

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate: Обновление ${appWidgetIds.size} виджетов")
        
        // Обновляем каждый виджет
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled: Первый виджет создан")
    }
    
    override fun onDisabled(context: Context) {
        Log.d(TAG, "onDisabled: Последний виджет удален")
    }
    
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d(TAG, "onDeleted: Удалено ${appWidgetIds.size} виджетов")
    }
    
    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        try {
            // Создаем базовый RemoteViews
            val views = RemoteViews(context.packageName, R.layout.basic_widget_layout)
            
            // Получаем текущий день
            val today = LocalDate.now()
            val dayName = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ru"))
            
            // Устанавливаем заголовок
            views.setTextViewText(R.id.widget_day, dayName)
            
            // Пытаемся получить расписание на сегодня
            val scheduleStorage = ScheduleStorage.getInstance(context)
            val schedule = scheduleStorage.schedule.value
            val currentDay = schedule?.week?.find { it.name == today.dayOfWeek }
            
            if (currentDay != null && currentDay.lessons.isNotEmpty()) {
                // Есть занятия на сегодня
                val lesson = currentDay.lessons.sortedBy { it.startTime }.firstOrNull()
                if (lesson != null) {
                    views.setTextViewText(R.id.widget_lesson, lesson.name)
                    views.setTextViewText(R.id.widget_time, 
                        "${lesson.startTime.hour}:${lesson.startTime.minute.toString().padStart(2, '0')} - " +
                        "${lesson.endTime.hour}:${lesson.endTime.minute.toString().padStart(2, '0')}")
                    views.setTextViewText(R.id.widget_location, lesson.auditorium)
                } else {
                    views.setTextViewText(R.id.widget_lesson, "Нет занятий")
                    views.setTextViewText(R.id.widget_time, "")
                    views.setTextViewText(R.id.widget_location, "")
                }
            } else {
                // Нет занятий на сегодня
                views.setTextViewText(R.id.widget_lesson, "Нет занятий")
                views.setTextViewText(R.id.widget_time, "")
                views.setTextViewText(R.id.widget_location, "")
            }
            
            // Настраиваем действие при нажатии на виджет
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            // Обновляем виджет
            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d(TAG, "Виджет $appWidgetId успешно обновлен")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при обновлении виджета: ${e.message}", e)
            
            // В случае ошибки показываем простое сообщение
            try {
                val fallbackViews = RemoteViews(context.packageName, R.layout.basic_widget_layout)
                fallbackViews.setTextViewText(R.id.widget_day, "Расписание")
                fallbackViews.setTextViewText(R.id.widget_lesson, "Нажмите, чтобы открыть")
                fallbackViews.setTextViewText(R.id.widget_time, "")
                fallbackViews.setTextViewText(R.id.widget_location, "")
                
                // Настраиваем действие при нажатии на виджет
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent, 
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                fallbackViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                
                appWidgetManager.updateAppWidget(appWidgetId, fallbackViews)
            } catch (e: Exception) {
                Log.e(TAG, "Критическая ошибка при создании запасного виджета: ${e.message}", e)
            }
        }
    }
}

