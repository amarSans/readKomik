package com.tugasmobile.readkomik.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Comik::class], version = 1, exportSchema = false)
abstract class DatabaseComic : RoomDatabase() {

    abstract fun comicDao(): ComicDao

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
