package com.example.schedulerapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalTime

data class Schedule(
    val id: Int = 1,
    val week: List<Day>
)

@Parcelize
data class Day(
    val name: DayOfWeek,
    val lessons: List<Lesson>
) : Parcelable

@Parcelize
data class Lesson(
    val name: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val auditorium: String,
    val type: LessonType,
    var notificationEnabled: Boolean = true,
    var notificationMinutesBefore: Int = 15
) : Parcelable

enum class LessonType {
    LECTURE, PRACTICAL, LABORATORY, OTHER
}

