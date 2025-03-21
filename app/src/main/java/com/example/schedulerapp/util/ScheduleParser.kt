package com.example.schedulerapp.util

import android.util.Log
import com.example.schedulerapp.data.*
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object ScheduleParser {
    private const val TAG = "ScheduleParser"
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun parseSchedule(jsonString: String): Schedule {
        Log.d(TAG, "Parsing JSON: $jsonString")
        val jsonObject = JSONObject(jsonString)
        val weekArray = jsonObject.getJSONArray("week")
        val days = mutableListOf<Day>()

        for (i in 0 until weekArray.length()) {
            val dayObject = weekArray.getJSONObject(i)
            val dayName = DayOfWeek.valueOf(dayObject.getString("name").uppercase())
            val lessonsArray = dayObject.getJSONArray("lessons")
            val lessons = mutableListOf<Lesson>()

            for (j in 0 until lessonsArray.length()) {
                val lessonObject = lessonsArray.getJSONObject(j)
                val startTimeString = lessonObject.getString("start_time")
                val endTimeString = lessonObject.getString("end_time")
                
                val lesson = Lesson(
                    name = lessonObject.getString("name"),
                    startTime = LocalTime.parse(startTimeString, timeFormatter),
                    endTime = LocalTime.parse(endTimeString, timeFormatter),
                    auditorium = lessonObject.getString("auditorium"),
                    type = LessonType.valueOf(lessonObject.getString("type").uppercase()),
                    notificationEnabled = lessonObject.optBoolean("notificationEnabled", true),
                    notificationMinutesBefore = lessonObject.optInt("notificationMinutesBefore", 15)
                )
                lessons.add(lesson)
                Log.d(TAG, "Parsed lesson: $lesson")
            }

            val day = Day(dayName, lessons)
            days.add(day)
            Log.d(TAG, "Parsed day: $day")
        }

        val schedule = Schedule(week = days)
        Log.d(TAG, "Parsed schedule: $schedule")
        return schedule
    }
}

