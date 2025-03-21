package com.example.schedulerapp.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.schedulerapp.data.Day
import com.example.schedulerapp.fragment.DayFragment
import java.time.DayOfWeek

class DayPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    private val TAG = "DayPagerAdapter"
    private var days: List<Day> = emptyList()

    override fun getItemCount(): Int = 7 // Always show 7 days

    override fun createFragment(position: Int): Fragment {
        val dayOfWeek = DayOfWeek.of(position + 1)
        val day = days.find { it.name == dayOfWeek } ?: Day(dayOfWeek, emptyList())
        Log.d(TAG, "Creating fragment for day: $day")
        return DayFragment.newInstance(day)
    }

    fun submitList(newDays: List<Day>) {
        Log.d(TAG, "Submitting new list of days: $newDays")
        days = newDays.sortedBy { it.name.value }
        notifyDataSetChanged()
    }
}

