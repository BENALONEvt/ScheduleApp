package com.example.schedulerapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulerapp.data.Lesson
import com.example.schedulerapp.databinding.ItemLessonBinding
import java.time.format.DateTimeFormatter
import java.util.Locale

class LessonAdapter : ListAdapter<Lesson, LessonAdapter.LessonViewHolder>(LessonDiffCallback()) {
    private val TAG = "LessonAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = getItem(position)
        Log.d(TAG, "Binding lesson at position $position: $lesson")
        holder.bind(lesson)
    }

    class LessonViewHolder(private val binding: ItemLessonBinding) : RecyclerView.ViewHolder(binding.root) {
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

        fun bind(lesson: Lesson) {
            binding.apply {
                textViewLessonName.text = lesson.name
                val formattedStartTime = lesson.startTime.format(timeFormatter)
                val formattedEndTime = lesson.endTime.format(timeFormatter)
                textViewLessonTime.text = "$formattedStartTime - $formattedEndTime"
                textViewLessonAuditorium.text = lesson.auditorium
                textViewLessonType.text = lesson.type.name
                Log.d("LessonViewHolder", "Binding lesson: ${lesson.name}, Time: $formattedStartTime - $formattedEndTime")
            }
        }
    }

    class LessonDiffCallback : DiffUtil.ItemCallback<Lesson>() {
        override fun areItemsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem.name == newItem.name && 
                   oldItem.startTime == newItem.startTime && 
                   oldItem.auditorium == newItem.auditorium
        }

        override fun areContentsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem == newItem
        }
    }
}

