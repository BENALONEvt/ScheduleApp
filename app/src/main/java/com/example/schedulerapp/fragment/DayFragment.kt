package com.example.schedulerapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.schedulerapp.adapter.LessonAdapter
import com.example.schedulerapp.data.Day
import com.example.schedulerapp.databinding.FragmentDayBinding

class DayFragment : Fragment() {
    private val TAG = "DayFragment"

    private var _binding: FragmentDayBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LessonAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LessonAdapter()
        binding.recyclerViewLessons.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@DayFragment.adapter
        }

        arguments?.getParcelable<Day>("day")?.let { day ->
            Log.d(TAG, "Displaying day: $day")
            binding.textViewDayName.text = day.name.toString()
            adapter.submitList(day.lessons)
            
            if (day.lessons.isEmpty()) {
                Log.d(TAG, "No lessons for this day")
                binding.textViewEmptyState.visibility = View.VISIBLE
                binding.recyclerViewLessons.visibility = View.GONE
            } else {
                Log.d(TAG, "Lessons found: ${day.lessons}")
                binding.textViewEmptyState.visibility = View.GONE
                binding.recyclerViewLessons.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(day: Day): DayFragment {
            return DayFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("day", day)
                }
            }
        }
    }
}

