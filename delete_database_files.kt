import java.io.File

fun main() {
    val filesToDelete = listOf(
        "app/src/main/java/com/example/schedulerapp/database/Converters.kt",
        "app/src/main/java/com/example/schedulerapp/database/ScheduleDao.kt",
        "app/src/main/java/com/example/schedulerapp/database/ScheduleDatabase.kt",
        "app/src/main/java/com/example/schedulerapp/db/Converters.kt",
        "app/src/main/java/com/example/schedulerapp/db/ScheduleDao.kt",
        "app/src/main/java/com/example/schedulerapp/db/ScheduleDatabase.kt",
        "app/src/main/java/com/example/schedulerapp/db/LessonListConverter.kt"
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
    
    // Also delete the database and db directories if they're empty
    val databaseDir = File("app/src/main/java/com/example/schedulerapp/database")
    if (databaseDir.exists() && databaseDir.isDirectory && databaseDir.list()?.isEmpty() == true) {
        if (databaseDir.delete()) {
            println("Deleted empty database directory")
        } else {
            println("Failed to delete database directory")
        }
    }
    
    val dbDir = File("app/src/main/java/com/example/schedulerapp/db")
    if (dbDir.exists() && dbDir.isDirectory && dbDir.list()?.isEmpty() == true) {
        if (dbDir.delete()) {
            println("Deleted empty db directory")
        } else {
            println("Failed to delete db directory")
        }
    }
}

