package com.tugasmobile.readkomik.data

import android.app.Application
import androidx.lifecycle.LiveData
import java.util.concurrent.ExecutorService

import com.tugasmobile.readkomik.data.database.ComicDao
import com.tugasmobile.readkomik.data.database.Comik
import com.tugasmobile.readkomik.data.database.DatabaseComic
import java.util.concurrent.Executors

class ComicRepository(application: Application) {
    private val comicDao: ComicDao

    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val db = DatabaseComic.getDatabase(application)
        comicDao = db.comicDao()

    }

    suspend fun insert(comic: Comik) {
         comicDao.insert(comic)
    }
    suspend fun deleteAll() {
        comicDao.deleteAll()
    }
    fun update(comic: Comik){
        executorService.execute {
            comicDao.update(comic)
        }
    }
    fun getAllComics(): LiveData<List<Comik>> {
        return comicDao.getAllComics()
    }
    suspend fun getComicById(id: Int): Comik? {
        return comicDao.getComicById(id)
    }
    suspend fun updateTotalHalaman(comicID: Int, total: Int) {
        comicDao.updateTotalHalaman(comicID, total)
    }
    suspend fun updateProgress(comicId: Int, page: Int){
        comicDao.updateProgress(comicId, page)

    }


}