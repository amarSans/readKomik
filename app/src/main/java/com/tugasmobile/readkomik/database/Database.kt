package com.tugasmobile.readkomik.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tugasmobile.readkomik.data.Comik
import com.tugasmobile.readkomik.data.FolderComik

@Database(entities = [Comik::class, FolderComik::class], version = 1, exportSchema = false)
abstract class DatabaseComic : RoomDatabase() {

    abstract fun comicDao(): ComicDao
    abstract fun folderDao(): FolderDao


    companion object {
        @Volatile
        private var INSTANCE: DatabaseComic? = null

        fun getDatabase(context: Context): DatabaseComic {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseComic::class.java,
                    "db_komik"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
