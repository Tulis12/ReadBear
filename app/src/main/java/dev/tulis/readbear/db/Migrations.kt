package dev.tulis.readbear.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `Snippet` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT,
                `path` TEXT NOT NULL,
                `bookId` INTEGER NOT NULL,
                `progress` INTEGER NOT NULL,
                FOREIGN KEY(`bookId`) REFERENCES `Book`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_Snippet_bookId`
            ON `Snippet` (`bookId`)
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `Quote` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `quote` TEXT NOT NULL,
                `description` TEXT,
                `bookId` INTEGER NOT NULL,
                `progress` INTEGER NOT NULL,
                FOREIGN KEY(`bookId`) REFERENCES `Book`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `index_Quote_bookId`
            ON `Quote` (`bookId`)
            """.trimIndent()
        )
    }
}