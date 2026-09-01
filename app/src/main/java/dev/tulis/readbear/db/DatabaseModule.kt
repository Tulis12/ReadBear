package dev.tulis.readbear.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.tulis.readbear.db.books.BookDao
import dev.tulis.readbear.db.comics.ComicDao
import dev.tulis.readbear.db.comics.bookmarks.ComicBookmarkDao
import dev.tulis.readbear.db.comics.pages.ComicPageDao
import dev.tulis.readbear.db.pdfs.PdfDao
import dev.tulis.readbear.db.pdfs.bookmarks.PdfBookmarkDao
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
            "reader.db"
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
    fun provideComicPageDao(
        database: AppDatabase
    ): ComicPageDao {
        return database.comicPageDao()
    }

    @Provides
    fun provideComicDao(
        database: AppDatabase
    ): ComicDao {
        return database.comicDao()
    }

    @Provides
    fun provideBookmarkDao(
        database: AppDatabase
    ): ComicBookmarkDao {
        return database.comicBookmarkDao()
    }

    @Provides
    fun providePdfDao(
        database: AppDatabase
    ): PdfDao {
        return database.pdfDao()
    }

    @Provides
    fun providePdfBookmarkDao(
        database: AppDatabase
    ): PdfBookmarkDao {
        return database.pdfBookmarkDao()
    }
}