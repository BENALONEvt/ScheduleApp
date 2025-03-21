package com.example.schedulerapp

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager2.widget.ViewPager2
import com.example.schedulerapp.adapter.DayPagerAdapter
import com.example.schedulerapp.databinding.ActivityMainBinding
import com.example.schedulerapp.util.CsvGenerator
import com.example.schedulerapp.util.CsvScheduleParser
import com.example.schedulerapp.util.ScheduleParser
import com.example.schedulerapp.viewmodel.ScheduleViewModel
import com.example.schedulerapp.widget.MinimalWidget
import com.google.android.material.tabs.TabLayoutMediator
import java.time.DayOfWeek
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val viewModel: ScheduleViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var pagerAdapter: DayPagerAdapter

    // File picker launcher for importing schedule
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val fileName = getFileName(uri)
                Log.d(TAG, "Selected file: $fileName")

                // Try to parse as CSV
                val schedule = CsvScheduleParser.parseSchedule(this, uri)
                viewModel.loadSchedule(schedule)

                Toast.makeText(this, "Расписание успешно загружено из файла $fileName", Toast.LENGTH_SHORT).show()

                // Update widgets
                updateWidgets()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading file: ${e.message}", e)
                Toast.makeText(this, "Ошибка загрузки файла: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Helper function to get file name from URI
    private fun getFileName(uri: Uri): String {
        var result = "unknown"
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        if (result == "unknown") {
            result = uri.path?.substringAfterLast('/')?.substringAfterLast('\\') ?: "unknown"
        }
        return result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Apply theme setting
            val prefs = getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)
            val isDarkTheme = prefs.getBoolean("dark_theme", false)
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setSupportActionBar(binding.toolbar)
            supportActionBar?.title = getString(R.string.app_name)

            setupViewPager()
            observeViewModel()

            // Отладочная информация о виджетах
            debugWidgets()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Ошибка инициализации приложения", Toast.LENGTH_LONG).show()
        }
    }

    private fun debugWidgets() {
        try {
            Log.d(TAG, "=== ОТЛАДКА ВИДЖЕТОВ ===")

            // Проверяем регистрацию виджета
            val widgetComponentName = ComponentName(this, MinimalWidget::class.java)
            try {
                val providerInfo = packageManager.getReceiverInfo(widgetComponentName, PackageManager.GET_META_DATA)
                Log.d(TAG, "Виджет зарегистрирован: ${providerInfo.name}")
                Log.d(TAG, "Экспортирован: ${providerInfo.exported}")

                val metaData = providerInfo.metaData
                if (metaData != null) {
                    val providerResource = metaData.getInt("android.appwidget.provider", 0)
                    Log.d(TAG, "Ресурс провайдера: $providerResource")
                } else {
                    Log.d(TAG, "Мета-данные отсутствуют")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при проверке регистрации виджета: ${e.message}", e)
            }

            // Проверяем существующие виджеты
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponentName)
            Log.d(TAG, "Количество виджетов: ${widgetIds.size}")

            // Проверяем доступность ресурсов
            try {
                val layoutId = resources.getIdentifier("minimal_widget", "layout", packageName)
                Log.d(TAG, "ID макета minimal_widget: $layoutId")

                val infoId = resources.getIdentifier("minimal_widget_info", "xml", packageName)
                Log.d(TAG, "ID info minimal_widget_info: $infoId")

                // Проверяем, что ресурсы действительно существуют
                if (layoutId == 0) {
                    Log.e(TAG, "ОШИБКА: Макет minimal_widget не найден!")
                } else {
                    Log.d(TAG, "Макет minimal_widget найден")
                }

                if (infoId == 0) {
                    Log.e(TAG, "ОШИБКА: XML minimal_widget_info не найден!")
                } else {
                    Log.d(TAG, "XML minimal_widget_info найден")
                }

                // Проверяем, что drawable widget_preview существует
                val previewId = resources.getIdentifier("widget_preview", "drawable", packageName)
                Log.d(TAG, "ID preview widget_preview: $previewId")
                if (previewId == 0) {
                    Log.e(TAG, "ОШИБКА: Drawable widget_preview не найден!")
                } else {
                    Log.d(TAG, "Drawable widget_preview найден")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при проверке ресурсов: ${e.message}", e)
            }

            Log.d(TAG, "=== КОНЕЦ ОТЛАДКИ ВИДЖЕТОВ ===")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при отладке виджетов: ${e.message}", e)
        }
    }

    private fun updateWidgets() {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val widgetComponentName = ComponentName(this, MinimalWidget::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponentName)

            if (widgetIds.isNotEmpty()) {
                val intent = Intent(this, MinimalWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                }
                sendBroadcast(intent)
                Log.d(TAG, "Отправлен запрос на обновление ${widgetIds.size} виджетов")
                Toast.makeText(this, "Виджеты обновлены", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "Нет виджетов для обновления")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при обновлении виджетов: ${e.message}", e)
            Toast.makeText(this, "Ошибка при обновлении виджетов", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewPager() {
        try {
            pagerAdapter = DayPagerAdapter(this)
            binding.viewPager.adapter = pagerAdapter

            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = DayOfWeek.of(position + 1).name.substring(0, 3)
            }.attach()

            binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    viewModel.setCurrentDay(position + 1)
                }
            })

            // Set initial day to current day of week
            val currentDayOfWeek = LocalDate.now().dayOfWeek.value
            viewModel.setCurrentDay(currentDayOfWeek)
            binding.viewPager.setCurrentItem(currentDayOfWeek - 1, false)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up ViewPager: ${e.message}")
        }
    }

    private fun observeViewModel() {
        try {
            viewModel.schedule.observe(this) { schedule ->
                if (schedule != null && schedule.week.isNotEmpty()) {
                    Log.d(TAG, "Observed schedule: $schedule")
                    pagerAdapter.submitList(schedule.week)

                    // Update widgets when schedule changes
                    updateWidgets()
                } else {
                    Log.d(TAG, "No schedule data, loading sample schedule")
                    loadSampleSchedule()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error observing ViewModel: ${e.message}")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_load_schedule -> {
                // Open file picker
                filePickerLauncher.launch("*/*")
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_export_schedule -> {
                exportScheduleToCsv()
                true
            }
            R.id.action_update_widgets -> {
                updateWidgets()
                debugWidgets()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadSampleSchedule() {
        try {
            // Try to use the CSV parser first
            val inputStream = assets.open("sample_schedule.csv")
            val schedule = CsvScheduleParser.parseCsvInputStream(inputStream)
            viewModel.loadSchedule(schedule)
            Toast.makeText(this, "Расписание успешно загружено", Toast.LENGTH_SHORT).show()

            // Update widgets
            updateWidgets()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sample CSV schedule", e)
            // Fallback to JSON if CSV parsing fails
            try {
                val jsonString = assets.open("sample_schedule.json").bufferedReader().use { it.readText() }
                Log.d(TAG, "Falling back to JSON: $jsonString")
                val schedule = ScheduleParser.parseSchedule(jsonString)
                viewModel.loadSchedule(schedule)
                Toast.makeText(this, "Расписание успешно загружено из JSON", Toast.LENGTH_SHORT).show()

                // Update widgets
                updateWidgets()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading sample schedule", e)
                Toast.makeText(this, "Ошибка загрузки расписания: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun exportScheduleToCsv() {
        try {
            viewModel.schedule.value?.let { schedule ->
                val uri = CsvGenerator.exportScheduleToCsv(this, schedule)
                if (uri != null) {
                    // Create a share intent
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = "text/csv"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Поделиться расписанием"))
                    Toast.makeText(this, "Расписание экспортировано", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Ошибка экспорта расписания", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Нет данных для экспорта", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting schedule: ${e.message}")
            Toast.makeText(this, "Ошибка экспорта расписания: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

