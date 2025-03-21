package com.example.schedulerapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.schedulerapp.MainActivity
import com.example.schedulerapp.R
import com.example.schedulerapp.data.ScheduleStorage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Минимальная реализация виджета
 */
class MinimalWidget : AppWidgetProvider() {
    private val TAG = "MinimalWidget"

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate вызван")

        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        try {
            // Создаем RemoteViews
            val views = RemoteViews(context.packageName, R.layout.minimal_widget)

            // Настраиваем действие при нажатии на виджет
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.minimal_widget_container, pendingIntent)

            // Настраиваем действие при нажатии на кнопку обновления
            val refreshIntent = Intent(context, MinimalWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)

            // Получаем текущую дату
            val today = LocalDate.now()
            val dayOfWeek = today.dayOfWeek

            // Устанавливаем день недели (полное название)
            val dayText = dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ru")).uppercase()
            views.setTextViewText(R.id.widget_day, dayText)

            // Получаем расписание
            val scheduleStorage = ScheduleStorage.getInstance(context)
            val schedule = scheduleStorage.schedule.value

            // Находим занятия на сегодня
            val todaySchedule = schedule?.week?.find { it.name == dayOfWeek }

            if (todaySchedule != null && todaySchedule.lessons.isNotEmpty()) {
                // Есть занятия на сегодня
                views.setViewVisibility(R.id.widget_lessons_container, View.VISIBLE)
                views.setViewVisibility(R.id.widget_empty_message, View.GONE)

                // Сортируем занятия по времени начала
                val sortedLessons = todaySchedule.lessons.sortedBy { it.startTime }

                // Заполняем данные о занятиях (максимум 3)
                val lessonCount = minOf(sortedLessons.size, 3)

                // Используем прямое обращение к ресурсам вместо getIdentifier
                for (i in 0 until lessonCount) {
                    val lesson = sortedLessons[i]
                    val lessonNumber = i + 1

                    // Форматируем время
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                    val startTime = lesson.startTime.format(timeFormatter)

                    // Устанавливаем данные для каждого урока отдельно, без использования getIdentifier
                    when (lessonNumber) {
                        1 -> {
                            views.setTextViewText(R.id.lesson_1_name, lesson.name)
                            views.setTextViewText(R.id.lesson_1_time, startTime)
                            views.setTextViewText(R.id.lesson_1_room, lesson.auditorium)
                            views.setViewVisibility(R.id.lesson_1, View.VISIBLE)
                        }
                        2 -> {
                            views.setTextViewText(R.id.lesson_2_name, lesson.name)
                            views.setTextViewText(R.id.lesson_2_time, startTime)
                            views.setTextViewText(R.id.lesson_2_room, lesson.auditorium)
                            views.setViewVisibility(R.id.lesson_2, View.VISIBLE)
                        }
                        3 -> {
                            views.setTextViewText(R.id.lesson_3_name, lesson.name)
                            views.setTextViewText(R.id.lesson_3_time, startTime)
                            views.setTextViewText(R.id.lesson_3_room, lesson.auditorium)
                            views.setViewVisibility(R.id.lesson_3, View.VISIBLE)
                        }
                    }
                }

                // Скрываем неиспользуемые строки
                for (i in lessonCount until 3) {
                    when (i + 1) {
                        1 -> views.setViewVisibility(R.id.lesson_1, View.GONE)
                        2 -> views.setViewVisibility(R.id.lesson_2, View.GONE)
                        3 -> views.setViewVisibility(R.id.lesson_3, View.GONE)
                    }
                }
            } else {
                // Нет занятий на сегодня
                views.setViewVisibility(R.id.widget_lessons_container, View.GONE)
                views.setViewVisibility(R.id.widget_empty_message, View.VISIBLE)
            }

            // Обновляем виджет
            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d(TAG, "Виджет $appWidgetId обновлен")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при обновлении виджета: ${e.message}", e)

            try {
                // В случае ошибки показываем простой виджет
                val views = RemoteViews(context.packageName, R.layout.minimal_widget)
                views.setViewVisibility(R.id.widget_lessons_container, View.GONE)
                views.setViewVisibility(R.id.widget_empty_message, View.VISIBLE)
                views.setTextViewText(R.id.widget_empty_message, "Ошибка загрузки расписания")

                // Настраиваем действие при нажатии
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent, PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.minimal_widget_container, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.e(TAG, "Критическая ошибка при обновлении виджета: ${e.message}", e)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Обрабатываем дополнительные действия, если нужно
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)

            if (appWidgetIds != null) {
                for (appWidgetId in appWidgetIds) {
                    updateWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }
}

