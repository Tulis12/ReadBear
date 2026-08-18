package dev.tulis.tulireader.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.tulis.tulireader.db.bookmarks.Bookmark
import dev.tulis.tulireader.db.bookmarks.BookmarkDao
import dev.tulis.tulireader.db.books.BookDao
import dev.tulis.tulireader.db.pages.PageDao
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