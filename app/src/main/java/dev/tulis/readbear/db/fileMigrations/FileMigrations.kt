package dev.tulis.readbear.db

import android.content.Context
import android.os.StatFs
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.tulis.readbear.R
import dev.tulis.readbear.db.entities.books.BookDao
import dev.tulis.readbear.db.fileMigrations.FileMigration
import dev.tulis.readbear.db.fileMigrations.FileMigrationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import androidx.core.content.edit
import dev.tulis.readbear.db.fileMigrations.CheckMigrationResult
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class FileMigrations @Inject constructor (
    private val bookDao: BookDao,
    val booksDir: File,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val availableMigrations: MutableList<FileMigration> = ArrayList()

    private val toMigrate: MutableList<FileMigration> = mutableStateListOf()

    private val prefs = context.getSharedPreferences(
        "storageMigrations",
        Context.MODE_PRIVATE
    )

    val fileMigrationToV1 = object : FileMigration(1) {
        val filesDir = context.filesDir

        fun sha256(file: File): String {
            val digest = MessageDigest.getInstance("SHA-256")

            file.inputStream().buffered().use { input ->
                val buffer = ByteArray(8192)
                while (true) {
                    val bytesRead = input.read(buffer)
                    if (bytesRead == -1) break
                    digest.update(buffer, 0, bytesRead)
                }
            }

            return digest.digest().joinToString("") { "%02x".format(it) }
        }

        fun areDirectoriesIdentical(dir1: File, dir2: File): Boolean {
            if (!dir1.isDirectory || !dir2.isDirectory) return false

            val files1 = dir1.walkTopDown()
                .filter { it.isFile }
                .associateBy { it.relativeTo(dir1).invariantSeparatorsPath }

            val files2 = dir2.walkTopDown()
                .filter { it.isFile }
                .associateBy { it.relativeTo(dir2).invariantSeparatorsPath }

            if (files1.keys != files2.keys) return false

            return files1.all { (path, file1) ->
                val file2 = files2[path]!!
                file1.length() == file2.length() && sha256(file1) == sha256(file2)
            }
        }

        fun isUuid(name: String): Boolean {
            return try {
                UUID.fromString(name)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        override fun checkSafeMigration(): CheckMigrationResult {
            val stat = StatFs(filesDir.path)
            val freeBytes = stat.availableBytes

            val usedBytes = filesDir.walkTopDown()
                .filter { it.isFile }
                .sumOf { it.length() }

            return CheckMigrationResult(
                freeBytes > usedBytes,
                context.getString(R.string.unsafe_v1_migration, (usedBytes - freeBytes) / (1024.0 * 1024 * 1024))
            )
        }

        override suspend fun migrate(state: FileMigrationState): Boolean {
            val sacredDirectories = arrayOf("datastore", "snippets", "books")
            val booksPaths: MutableList<String> = ArrayList()

            val books = bookDao.getAll()
            books.forEach {
                booksPaths.add(it.path)
            }

            state.message = context.getString(R.string.copying)

            val directories = filesDir.walkTopDown()
                .onEnter {
                    !sacredDirectories.contains(it.name)
                }
                .filter {
                    it.isDirectory && isUuid(it.name)
                }
                .toList()

            val total = directories.count().toFloat()
            var progress = 0

            directories.forEach {
                if (!booksPaths.contains(it.name)) {
                    it.deleteRecursively()

                    progress++
                    state.progress = progress / total
                    return@forEach
                }

                val newFolder = File(booksDir, it.name)
                newFolder.mkdirs()

                it.copyRecursively(newFolder, overwrite = true)

                progress++
                state.progress = progress / total
            }

            progress = 0
            state.message = context.getString(R.string.verifying)
            state.progress = 0f

            var success = true

            booksPaths.forEach {
                val oldDir = filesDir.resolve(it)
                val newDir = booksDir.resolve(it)

                if(!areDirectoriesIdentical(newDir, oldDir)) {
                    success = false
                }

                progress++
                state.progress = progress / booksPaths.count().toFloat()
            }

            state.progress = 1f

            if(success) {
                state.message = context.getString(R.string.success)

                booksDir.listFiles()?.forEach {
                    if(!it.isDirectory) return@forEach

                    val oldDir = filesDir.resolve(it.name)
                    oldDir.deleteRecursively()
                }
            }

            return success
        }

        override suspend fun unsafeMigrate(state: FileMigrationState): Boolean {
            val sacredDirectories = arrayOf("datastore", "snippets", "books")
            val booksPaths: MutableList<String> = ArrayList()

            val books = bookDao.getAll()
            books.forEach {
                booksPaths.add(it.path)
            }

            state.message = context.getString(R.string.moving)

            val directories = filesDir.walkTopDown()
                .onEnter {
                    !sacredDirectories.contains(it.name)
                }
                .filter {
                    it.isDirectory && isUuid(it.name)
                }
                .toList()

            val total = directories.count().toFloat()
            var progress = 0

            var success = true

            directories.forEach {
                if(!success) {
                    progress++
                    state.progress = progress / total
                    return@forEach
                }

                if (!booksPaths.contains(it.name)) {
                    it.deleteRecursively()

                    progress++
                    state.progress = progress / total
                    return@forEach
                }

                val newFolder = File(booksDir, it.name)

                if (!it.renameTo(newFolder)) {
                    success = false
                    return@forEach
                }

                progress++
                state.progress = progress / total
            }

            if(success) {
                state.message = context.getString(R.string.success)
            }

            delay(1500.milliseconds)

            state.progress = 0f
            return success
        }
    }

    init {
        loadMigrations()

        val version = prefs.getInt("version", 0)

        for(migration in availableMigrations) {
            if(version < migration.dest) {
                toMigrate.add(migration)
            }
        }
    }

    fun loadMigrations() {
        availableMigrations.add(fileMigrationToV1)
//        availableMigrations.add(dummyMigration)
    }

    fun hasMigrations(): Boolean {
        return !toMigrate.isEmpty()
    }

    fun isSafeToMigrateAll(): Boolean {
        var safeMigration = true

        for(migration in toMigrate) {
            if(!migration.checkSafeMigration().isSafe) safeMigration = false
        }

        return safeMigration
    }

    fun migrate(state: FileMigrationState) {
        viewModelScope.launch {
            state.stageCurrent = 1
            state.stageMax = toMigrate.count()

            for (migration in toMigrate.toMutableList()) {
                withContext(Dispatchers.IO) {
                    migration.migrate(state)
                }

                state.stageCurrent++
                toMigrate.remove(migration)

                prefs.edit {
                    putInt("version", migration.dest)
                }
            }
        }
    }

    fun getUnsafeMigrations(): List<FileMigration> {
        val unsafeMigrations = mutableListOf<FileMigration>()

        for(migration in toMigrate) {
            if(!migration.checkSafeMigration().isSafe) {
                unsafeMigrations.add(migration)
            }
        }

        return unsafeMigrations
    }

    suspend fun migrateAllUnsafe(state: FileMigrationState, unsafeMigrations: List<FileMigration>) {
        for (migration in unsafeMigrations.toMutableList()) {
            withContext(Dispatchers.IO) {
                migration.unsafeMigrate(state)
            }

            state.stageCurrent++
            toMigrate.remove(migration)

            prefs.edit {
                putInt("version", migration.dest)
            }
        }
    }
}

