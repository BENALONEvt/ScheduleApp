import java.io.File

fun main() {
    val filesToDelete = listOf(
        "app/src/main/java/com/example/schedulerapp/model/Day.kt",
        "app/src/main/java/com/example/schedulerapp/model/Lesson.kt",
        "app/src/main/java/com/example/schedulerapp/model/Schedule.kt",
        "app/src/main/java/com/example/schedulerapp/model/ScheduleModel.kt",
        "app/src/main/java/com/example/schedulerapp/model/LessonType.kt"
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
    
    // Also delete the model directory if it's empty
    val modelDir = File("app/src/main/java/com/example/schedulerapp/model")
    if (modelDir.exists() && modelDir.isDirectory && modelDir.list()?.isEmpty() == true) {
        if (modelDir.delete()) {
            println("Deleted empty model directory")
        } else {
            println("Failed to delete model directory")
        }
    }
}

