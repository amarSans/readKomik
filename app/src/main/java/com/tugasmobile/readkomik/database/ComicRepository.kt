package com.tugasmobile.readkomik.database

import android.app.Application
import androidx.lifecycle.LiveData
import com.tugasmobile.readkomik.data.Comik
import com.tugasmobile.readkomik.data.FolderComik
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ComicRepository(application: Application) {
    private val comicDao: ComicDao

    private val folderDao: FolderDao


    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val db = DatabaseComic.getDatabase(application)
        comicDao = db.comicDao()
        folderDao = db.folderDao()

    }
    suspend fun insertFolder(folder: FolderComik): Int{
        return folderDao.insertFolder(folder).toInt()
    }

    fun getAllFolders(): LiveData<List<FolderComik>> {
        return folderDao.getAllFolders()
    }

    suspend fun deleteFolder(folder: FolderComik) {
        folderDao.delete(folder)
    }

    suspend fun update(folder: FolderComik) {
        folderDao.update(folder)
    }

    suspend fun getFolderByPath(path: String): FolderComik? {
        return folderDao.getFolderByPath(path)
    }

    fun getComicByFolder(folderId: Int): LiveData<List<Comik>> {
        return comicDao.getComicByFolder(folderId)
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

    suspend fun getComicByUrl(url: String): Comik? {
        return comicDao.getComicByUrl(url)
    }

    suspend fun getFolderById(idFolder: Int): FolderComik? {
        return folderDao.getFolderById(idFolder)
    }




}