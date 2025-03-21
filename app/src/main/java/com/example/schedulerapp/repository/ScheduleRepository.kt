package com.example.schedulerapp.repository

import androidx.lifecycle.LiveData
import com.example.schedulerapp.data.Schedule
import com.example.schedulerapp.data.ScheduleStorage

class ScheduleRepository(private val scheduleStorage: ScheduleStorage) {
    val schedule: LiveData<Schedule> = scheduleStorage.schedule

    suspend fun insertSchedule(schedule: Schedule) {
        scheduleStorage.saveSchedule(schedule)
    }
}

