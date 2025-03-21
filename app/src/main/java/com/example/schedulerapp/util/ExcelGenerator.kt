package com.example.schedulerapp.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.schedulerapp.data.Schedule
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.write.Label
import jxl.write.WritableWorkbook
import jxl.write.WritableCellFormat
import jxl.write.WritableFont
import jxl.write.WritableSheet
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility class for generating Excel files from Schedule data
 */
object ExcelGenerator {
    private const val TAG = "ExcelGenerator"
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * Generates an Excel file from a Schedule object and returns the Uri
     */
    fun generateExcelFile(context: Context, schedule: Schedule): Uri? {
        try {
            // Set up workbook settings
            val workbookSettings = WorkbookSettings()
            workbookSettings.locale = Locale.getDefault()
            
            // Create file for output
            val fileName = "schedule.xls"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            // Create a writable workbook
            val workbook = Workbook.createWorkbook(file, workbookSettings)
            
            // Create header font and format
            val headerFont = WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD)
            val headerFormat = WritableCellFormat(headerFont)
            
            // Process each day in the schedule
            schedule.week.forEachIndexed { index, day ->
                // Create a sheet for each day
                val sheet = workbook.createSheet(day.name.toString(), index)
                
                // Add headers
                val headers = arrayOf("Name", "Start Time", "End Time", "Auditorium", "Type")
                headers.forEachIndexed { colIndex, header ->
                    sheet.addCell(Label(colIndex, 0, header, headerFormat))
                }
                
                // Add lessons to the sheet
                day.lessons.forEachIndexed { rowIndex, lesson ->
                    sheet.addCell(Label(0, rowIndex + 1, lesson.name))
                    sheet.addCell(Label(1, rowIndex + 1, lesson.startTime.format(timeFormatter)))
                    sheet.addCell(Label(2, rowIndex + 1, lesson.endTime.format(timeFormatter)))
                    sheet.addCell(Label(3, rowIndex + 1, lesson.auditorium))
                    sheet.addCell(Label(4, rowIndex + 1, lesson.type.name))
                }
                
                // Auto-size columns (not directly supported in JExcelAPI, but we can set reasonable widths)
                for (i in 0..4) {
                    when (i) {
                        0 -> sheet.setColumnView(i, 30) // Name column
                        1, 2 -> sheet.setColumnView(i, 10) // Time columns
                        3 -> sheet.setColumnView(i, 15) // Auditorium column
                        4 -> sheet.setColumnView(i, 12) // Type column
                    }
                }
            }
            
            // Write and close the workbook
            workbook.write()
            workbook.close()
            
            // Get content URI via FileProvider
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating Excel file", e)
            return null
        }
    }
    
    /**
     * Exports the current schedule to an Excel file and returns the Uri
     */
    fun exportScheduleToExcel(context: Context, schedule: Schedule): Uri? {
        return generateExcelFile(context, schedule)
    }
}

