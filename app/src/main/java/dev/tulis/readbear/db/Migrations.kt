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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            DROP TABLE `Quote`;
            )
            """.trimIndent()
        )

        val now = System.currentTimeMillis()

        db.execSQL("ALTER TABLE Book ADD COLUMN lastReadAt INTEGER")
        db.execSQL("ALTER TABLE Pdf ADD COLUMN pdfCreationDate INTEGER")

        db.execSQL("ALTER TABLE Book ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE Snippet ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")

        db.execSQL("UPDATE Book SET createdAt = $now WHERE createdAt = 0")
        db.execSQL("UPDATE Snippet SET createdAt = $now WHERE createdAt = 0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE "BookFolderItem" (
                "id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                "parentId" INTEGER,
                "type" TEXT NOT NULL,
                "positionIndex" INTEGER NOT NULL,
                FOREIGN KEY("parentId") REFERENCES "BookFolder"("id")
                    ON DELETE SET NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE "BookFolder" (
                "id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                "name" TEXT NOT NULL,
                "description" TEXT,
                "cover" TEXT,
                "itemId" INTEGER NOT NULL,
                FOREIGN KEY("itemId") REFERENCES "BookFolderItem"("id")
                    ON DELETE CASCADE
            );
            """.trimIndent()
        )

        db.execSQL("""
            CREATE INDEX `index_BookFolder_itemId` ON `BookFolder` (`itemId`);
        """.trimIndent())

        db.execSQL("""
            CREATE INDEX `index_BookFolderItem_itemId` ON `BookFolderItem` (`parentId`);
        """.trimIndent())

        db.execSQL(
            """
                ALTER TABLE Book ADD COLUMN folderItemId INTEGER REFERENCES BookFolderItem(id)
            """.trimIndent()
        )

        val cursor = db.query("SELECT id FROM Book")

        cursor.use {
            var i = 0

            while (it.moveToNext()) {
                var bookId = it.getLong(0)

                db.execSQL(
                    "INSERT INTO BookFolderItem(parentId, positionIndex, type) VALUES (?, ?, 'Book')",
                    arrayOf(null, i)
                )

                val itemId = db.query("SELECT last_insert_rowid()").use { cursor ->
                    check(cursor.moveToFirst())
                    cursor.getLong(0)
                }

                db.execSQL(
                    "UPDATE Book SET folderItemId = ? WHERE id = ?",
                    arrayOf(itemId, bookId)
                )

                i++
            }
        }

        db.execSQL("""
            CREATE TABLE `_Book` (`id`            INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title`         TEXT NOT NULL,
                    `summary`       TEXT,
                    `author`        TEXT,
                    `published`     TEXT,
                    `web`           TEXT,
                    `path`          TEXT NOT NULL,
                    `cover`         TEXT NOT NULL,
                    `type`          TEXT NOT NULL,
                    `readingTime`   INTEGER NOT NULL,
                    `progress`      INTEGER NOT NULL,
                    `totalProgress` INTEGER NOT NULL,
                    `readAlready`   INTEGER NOT NULL,
                    `folderItemId`  INTEGER NOT NULL REFERENCES BookFolderItem(id) ON DELETE CASCADE,
                    `lastReadAt`    INTEGER,
                    `createdAt`     INTEGER NOT NULL
            )
            """.trimIndent())

        db.execSQL("""
            INSERT INTO _Book (
                id, title, summary, author, published, web, path, cover, type,
                readingTime, progress, totalProgress, readAlready, lastReadAt, createdAt, folderItemId
            )
            SELECT
                id, title, summary, author, published, web, path, cover, type,
                readingTime, progress, totalProgress, readAlready, lastReadAt, createdAt, folderItemId
            FROM Book;
        """.trimIndent())

        db.execSQL("DROP TABLE Book")
        db.execSQL("AlTER TABLE _Book RENAME TO Book")
    }
}