package com.example.schedulerapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.schedulerapp.adapter.DayPagerAdapter
import com.example.schedulerapp.databinding.ActivitySplashBinding
import com.example.schedulerapp.viewmodel.ScheduleViewModel
import java.time.LocalDate

class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity"
    private lateinit var binding: ActivitySplashBinding
    private lateinit var viewModel: ScheduleViewModel
    private lateinit var pagerAdapter: DayPagerAdapter
    private val handler = Handler(Looper.getMainLooper())
    
    // Minimum splash screen duration
    private val MIN_SPLASH_DURATION = 2000L
    private var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            startTime = System.currentTimeMillis()

            // Start logo animation
            startLogoAnimation()
            
            try {
                // Initialize ViewModel
                viewModel = ViewModelProvider(this)[ScheduleViewModel::class.java]
                
                // Initialize ViewPager in background
                initializeViewPager()
                
                // Load schedule data
                viewModel.loadScheduleIfNeeded()
                
                // Observe schedule data
                viewModel.schedule.observe(this) { schedule ->
                    if (schedule != null && schedule.week.isNotEmpty()) {
                        try {
                            pagerAdapter.submitList(schedule.week)
                            // Start pager animation after data is loaded
                            handler.postDelayed({ animateViewPager() }, 500)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error updating adapter: ${e.message}")
                            ensureMinimumSplashDuration()
                        }
                    } else {
                        // If no schedule data, just go to main activity after delay
                        ensureMinimumSplashDuration()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing ViewModel or ViewPager: ${e.message}")
                ensureMinimumSplashDuration()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in onCreate: ${e.message}")
            // If we can't even initialize the binding, just start MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    
    private fun ensureMinimumSplashDuration() {
        val elapsedTime = System.currentTimeMillis() - startTime
        val remainingTime = (MIN_SPLASH_DURATION - elapsedTime).coerceAtLeast(0)
        
        handler.postDelayed({ launchMainActivity() }, remainingTime)
    }
    
    private fun startLogoAnimation() {
        try {
            // Rotate animation
            val rotation = ObjectAnimator.ofFloat(binding.imageLogo, View.ROTATION, 0f, 360f)
            rotation.duration = 2000
            rotation.repeatCount = 1
            rotation.interpolator = AccelerateDecelerateInterpolator()
            
            // Scale animation
            val scaleX = ObjectAnimator.ofFloat(binding.imageLogo, View.SCALE_X, 1f, 1.2f, 1f)
            val scaleY = ObjectAnimator.ofFloat(binding.imageLogo, View.SCALE_Y, 1f, 1.2f, 1f)
            scaleX.duration = 2000
            scaleY.duration = 2000
            
            // Start animations
            rotation.start()
            scaleX.start()
            scaleY.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error in logo animation: ${e.message}")
        }
    }
    
    private fun initializeViewPager() {
        try {
            pagerAdapter = DayPagerAdapter(this)
            binding.viewPager.adapter = pagerAdapter
            binding.viewPager.offscreenPageLimit = 7
            
            // Hide ViewPager initially but keep it functional
            binding.viewPager.alpha = 0f
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ViewPager: ${e.message}")
        }
    }
    
    private fun animateViewPager() {
        try {
            val currentDayOfWeek = LocalDate.now().dayOfWeek.value
            
            // Set to current day first
            binding.viewPager.setCurrentItem(currentDayOfWeek - 1, false)
            
            // Animate to Sunday (index 6)
            binding.viewPager.setCurrentItem(6, true)
            
            // After reaching Sunday, go back to current day
            handler.postDelayed({
                binding.viewPager.setCurrentItem(currentDayOfWeek - 1, true)
                
                // After animation completes, launch main activity
                handler.postDelayed({
                    launchMainActivity()
                }, 500)
            }, 1000)
        } catch (e: Exception) {
            Log.e(TAG, "Error animating ViewPager: ${e.message}")
            launchMainActivity()
        }
    }
    
    private fun launchMainActivity() {
        try {
            // Fade out animation for the splash screen
            val fadeOut = ObjectAnimator.ofFloat(binding.splashContainer, View.ALPHA, 1f, 0f)
            fadeOut.duration = 500
            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    startMainActivity()
                }
            })
            fadeOut.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error in fade animation: ${e.message}")
            startMainActivity()
        }
    }
    
    private fun startMainActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
    
    override fun onBackPressed() {
        // Disable back button during splash screen
        // Do nothing
    }
}

