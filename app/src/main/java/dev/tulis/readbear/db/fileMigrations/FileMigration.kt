package dev.tulis.readbear.db.fileMigrations

abstract class FileMigration(
    val dest: Int
) {
    abstract fun checkSafeMigration(): CheckMigrationResult
    abstract suspend fun migrate(state: FileMigrationState): Boolean
    abstract suspend fun unsafeMigrate(state: FileMigrationState): Boolean
}

data class CheckMigrationResult(
    var isSafe: Boolean,
    var unsafeMirateMessage: String
)