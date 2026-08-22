package dev.tulis.readbear.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.tulis.readbear.db.bookmarks.BookmarkDao
import dev.tulis.readbear.db.books.BookDao
import dev.tulis.readbear.db.pages.PageDao
import jakarta.inject.Singleton
import java.io.File

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "books.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFilesDir(
        @ApplicationContext context: Context
    ): File {
        return context.filesDir
    }

    @Provides
    fun provideBookDao(
        database: AppDatabase
    ): BookDao {
        return database.bookDao()
    }

    @Provides
    fun providePageDao(
        database: AppDatabase
    ): PageDao {
        return database.pageDao()
    }

    @Provides
    fun provideBookmarkDao(
        database: AppDatabase
    ): BookmarkDao {
        return database.bookmarkDao()
    }
}