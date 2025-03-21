package com.example.schedulerapp.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.schedulerapp.data.Schedule
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.time.format.DateTimeFormatter

/**
* Utility class for generating CSV files from Schedule data
*/
object CsvGenerator {
    private const val TAG = "CsvGenerator"
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * Generates a CSV file from a Schedule object and returns the Uri
     */
    fun generateCsvFile(context: Context, schedule: Schedule): Uri? {
        try {
            // Create file for output
            val fileName = "schedule.csv"
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            
            // Ensure directory exists
            if (directory == null) {
                Log.e(TAG, "External files directory is null")
                return null
            }
            
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    Log.e(TAG, "Failed to create directory: ${directory.absolutePath}")
                    return null
                }
            }
            
            val file = File(directory, fileName)
            Log.d(TAG, "Creating CSV file at: ${file.absolutePath}")
            
            // Create writer
            val outputStream = FileOutputStream(file)
            val writer = OutputStreamWriter(outputStream)
            
            // Write header
            writer.write("Day,Name,Start Time,End Time,Auditorium,Type\n")
            
            // Process each day in the schedule
            schedule.week.forEach { day ->
                // Add lessons to the CSV
                day.lessons.forEach { lesson ->
                    val line = "${day.name}," +
                            "${escapeCSV(lesson.name)}," +
                            "${lesson.startTime.format(timeFormatter)}," +
                            "${lesson.endTime.format(timeFormatter)}," +
                            "${escapeCSV(lesson.auditorium)}," +
                            "${lesson.type.name}\n"
                    writer.write(line)
                }
            }
            
            // Close the writer
            writer.flush()
            writer.close()
            
            Log.d(TAG, "CSV file generated at: ${file.absolutePath}")
            
            // Get content URI via FileProvider
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating CSV file", e)
            return null
        }
    }
    
    /**
     * Escapes special characters in CSV fields
     */
    private fun escapeCSV(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }
    
    /**
     * Exports the current schedule to a CSV file and returns the Uri
     */
    fun exportScheduleToCsv(context: Context, schedule: Schedule): Uri? {
        return generateCsvFile(context, schedule)
    }
}

