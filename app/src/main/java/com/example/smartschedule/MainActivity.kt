package com.example.smartschedule

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.schedulerapp.MainActivity as SchedulerMainActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Redirect to the correct MainActivity
        val intent = Intent(this, SchedulerMainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

