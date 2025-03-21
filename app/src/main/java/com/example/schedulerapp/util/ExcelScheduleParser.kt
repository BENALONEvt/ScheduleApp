package com.example.schedulerapp.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.schedulerapp.data.*
import jxl.Sheet
import jxl.Workbook
import jxl.WorkbookSettings
import java.io.InputStream
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object ExcelScheduleParser {
    private const val TAG = "ExcelScheduleParser"
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun parseSchedule(context: Context, uri: Uri): Schedule {
        Log.d(TAG, "Parsing Excel file from URI: $uri")
        
        val inputStream = context.contentResolver.openInputStream(uri)
        return parseExcelInputStream(inputStream)
    }
    
    fun parseExcelInputStream(inputStream: InputStream?): Schedule {
        if (inputStream == null) {
            Log.e(TAG, "Input stream is null")
            throw IllegalArgumentException("Cannot read from null input stream")
        }
        
        val days = mutableListOf<Day>()
        
        try {
            // Set up workbook settings
            val workbookSettings = WorkbookSettings()
            workbookSettings.locale = Locale.getDefault()
            
            // Create workbook from input stream
            val workbook = Workbook.getWorkbook(inputStream, workbookSettings)
            
            // Process each sheet as a day of the week
            for (i in 0 until workbook.numberOfSheets) {
                val sheet = workbook.getSheet(i)
                val sheetName = sheet.name.uppercase()
                
                // Try to parse the sheet name as a day of week
                val dayOfWeek = try {
                    DayOfWeek.valueOf(sheetName)
                } catch (e: IllegalArgumentException) {
                    // If sheet name is not a valid day of week, use the index + 1
                    // (1 = Monday, 2 = Tuesday, etc.)
                    DayOfWeek.of((i % 7) + 1)
                }
                
                val lessons = parseSheetLessons(sheet)
                
                val day = Day(dayOfWeek, lessons)
                days.add(day)
                Log.d(TAG, "Parsed day: $day with ${lessons.size} lessons")
            }
            
            workbook.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Excel file", e)
            throw e
        } finally {
            try {
                inputStream.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing input stream", e)
            }
        }
        
        // Ensure we have entries for all 7 days of the week
        val existingDays = days.map { it.name }
        for (dayValue in 1..7) {
            val dayOfWeek = DayOfWeek.of(dayValue)
            if (!existingDays.contains(dayOfWeek)) {
                days.add(Day(dayOfWeek, emptyList()))
            }
        }
        
        val schedule = Schedule(week = days)
        Log.d(TAG, "Parsed schedule with ${days.size} days")
        return schedule
    }
    
    private fun parseSheetLessons(sheet: Sheet): List<Lesson> {
        val lessons = mutableListOf<Lesson>()
        
        // Skip header row (row 0) if it exists
        val startRow = if (sheet.getCell(0, 0).contents.contains("name", ignoreCase = true)) 1 else 0
        
        // Process each row as a lesson
        for (rowIndex in startRow until sheet.rows) {
            // Skip empty rows
            if (sheet.getCell(0, rowIndex).contents.isBlank()) {
                continue
            }
            
            try {
                // Extract lesson data from columns
                val name = sheet.getCell(0, rowIndex).contents
                
                // Parse start time
                val startTimeStr = sheet.getCell(1, rowIndex).contents
                val startTime = try {
                    LocalTime.parse(startTimeStr, timeFormatter)
                } catch (e: DateTimeParseException) {
                    Log.w(TAG, "Could not parse start time: $startTimeStr, using 00:00")
                    LocalTime.of(0, 0)
                }
                
                // Parse end time
                val endTimeStr = sheet.getCell(2, rowIndex).contents
                val endTime = try {
                    LocalTime.parse(endTimeStr, timeFormatter)
                } catch (e: DateTimeParseException) {
                    Log.w(TAG, "Could not parse end time: $endTimeStr, using 00:00")
                    LocalTime.of(0, 0)
                }
                
                val auditorium = sheet.getCell(3, rowIndex).contents
                
                // Parse lesson type
                val typeStr = sheet.getCell(4, rowIndex).contents.uppercase()
                val type = try {
                    LessonType.valueOf(typeStr)
                } catch (e: IllegalArgumentException) {
                    Log.w(TAG, "Unknown lesson type: $typeStr, defaulting to LECTURE")
                    LessonType.LECTURE
                }
                
                // Create lesson and add to list
                val lesson = Lesson(
                    name = name,
                    startTime = startTime,
                    endTime = endTime,
                    auditorium = auditorium,
                    type = type,
                    notificationEnabled = true,
                    notificationMinutesBefore = 15
                )
                
                lessons.add(lesson)
                Log.d(TAG, "Parsed lesson: $lesson")
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing row $rowIndex", e)
                // Continue with next row
            }
        }
        
        return lessons
    }
    
    // Helper method to create a sample Excel file for testing
    fun createSampleExcelInputStream(context: Context): InputStream {
        // For simplicity, we'll use the sample JSON and convert it
        // In a real implementation, you would include a sample Excel file in assets
        return context.assets.open("sample_schedule.json")
    }
}

