import java.io.File

fun main() {
    val filesToDelete = listOf(
        "app/src/main/java/com/example/schedulerapp/database/Converters.kt",
        "app/src/main/java/com/example/schedulerapp/database/ScheduleDao.kt",
        "app/src/main/java/com/example/schedulerapp/database/ScheduleDatabase.kt",
        "app/src/main/java/com/example/schedulerapp/db/Converters.kt",
        "app/src/main/java/com/example/schedulerapp/db/ScheduleDao.kt",
        "app/src/main/java/com/example/schedulerapp/db/ScheduleDatabase.kt",
        "app/src/main/java/com/example/schedulerapp/model/Day.kt",
        "app/src/main/java/com/example/schedulerapp/model/Lesson.kt",
        "app/src/main/java/com/example/schedulerapp/model/Schedule.kt",
        "app/src/main/java/com/example/schedulerapp/model/ScheduleModel.kt",
        "app/src/main/java/com/example/schedulerapp/storage/ScheduleStorage.kt"
    )

    filesToDelete.forEach { path ->
        val file = File(path)
        if (file.exists()) {
            if (file.delete()) {
                println("Deleted: $path")
            } else {
                println("Failed to delete: $path")
            }
        } else {
            println("File not found: $path")
        }
    }
}

