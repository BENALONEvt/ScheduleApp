package com.example.schedulerapp.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.schedulerapp.data.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object CsvScheduleParser {
    private const val TAG = "CsvScheduleParser"
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun parseSchedule(context: Context, uri: Uri): Schedule {
        Log.d(TAG, "Parsing CSV file from URI: $uri")
        
        val inputStream = context.contentResolver.openInputStream(uri)
        return parseCsvInputStream(inputStream)
    }
    
    fun parseCsvInputStream(inputStream: InputStream?): Schedule {
        if (inputStream == null) {
            Log.e(TAG, "Input stream is null")
            throw IllegalArgumentException("Cannot read from null input stream")
        }
        
        val days = mutableMapOf<DayOfWeek, MutableList<Lesson>>()
        
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            
            // Skip header if present
            var firstLine = reader.readLine()
            if (firstLine?.startsWith("Day,Name,Start Time,End Time,Auditorium,Type") == true) {
                // This is a header, skip it
                firstLine = reader.readLine()
            }
            
            // Process first line if it wasn't a header
            if (firstLine != null) {
                processLine(firstLine, days)
            }
            
            // Process remaining lines
            while (reader.readLine().also { line = it } != null) {
                processLine(line!!, days)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing CSV file", e)
            throw e
        } finally {
            try {
                inputStream.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing input stream", e)
            }
        }
        
        // Ensure we have entries for all 7 days of the week
        val weekDays = mutableListOf<Day>()
        for (dayValue in 1..7) {
            val dayOfWeek = DayOfWeek.of(dayValue)
            val lessons = days[dayOfWeek] ?: mutableListOf()
            weekDays.add(Day(dayOfWeek, lessons))
        }
        
        val schedule = Schedule(week = weekDays)
        Log.d(TAG, "Parsed schedule with ${weekDays.size} days")
        return schedule
    }
    
    private fun processLine(line: String, days: MutableMap<DayOfWeek, MutableList<Lesson>>) {
        try {
            val parts = line.split(",").map { it.trim() }
            if (parts.size < 6) {
                Log.w(TAG, "Invalid CSV line, not enough fields: $line")
                return
            }
            
            // Parse day of week
            val dayOfWeek = try {
                DayOfWeek.valueOf(parts[0].uppercase())
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Invalid day of week: ${parts[0]}, skipping line")
                return
            }
            
            // Parse lesson data
            val name = parts[1]
            
            // Parse start time
            val startTime = try {
                LocalTime.parse(parts[2], timeFormatter)
            } catch (e: DateTimeParseException) {
                Log.w(TAG, "Could not parse start time: ${parts[2]}, using 00:00")
                LocalTime.of(0, 0)
            }
            
            // Parse end time
            val endTime = try {
                LocalTime.parse(parts[3], timeFormatter)
            } catch (e: DateTimeParseException) {
                Log.w(TAG, "Could not parse end time: ${parts[3]}, using 00:00")
                LocalTime.of(0, 0)
            }
            
            val auditorium = parts[4]
            
            // Parse lesson type
            val type = try {
                LessonType.valueOf(parts[5].uppercase())
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Unknown lesson type: ${parts[5]}, defaulting to LECTURE")
                LessonType.LECTURE
            }
            
            // Create lesson
            val lesson = Lesson(
                name = name,
                startTime = startTime,
                endTime = endTime,
                auditorium = auditorium,
                type = type,
                notificationEnabled = true,
                notificationMinutesBefore = 15
            )
            
            // Add lesson to the appropriate day
            val dayLessons = days.getOrPut(dayOfWeek) { mutableListOf() }
            dayLessons.add(lesson)
            Log.d(TAG, "Parsed lesson for $dayOfWeek: $lesson")
        } catch (e: Exception) {
            Log.e(TAG, "Error processing CSV line: $line", e)
            // Continue with next line
        }
    }
    
    // Helper method to create a sample CSV file for testing
    fun createSampleCsvInputStream(context: Context): InputStream {
        // For simplicity, we'll use the sample JSON and convert it
        // In a real implementation, you would include a sample CSV file in assets
        return context.assets.open("sample_schedule.csv")
    }
}

