package com.tugasmobile.readkomik.page.comic


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.tugasmobile.readkomik.database.ComicRepository
import com.tugasmobile.readkomik.data.Comik
import com.tugasmobile.readkomik.data.FolderComik
import kotlinx.coroutines.launch

class ComicViewModel(application: Application): AndroidViewModel(application) {
    private val mComicRepository: ComicRepository =
        ComicRepository(application)
    fun getComicsByFolder(folderId: Int): LiveData<List<Comik>> {
        return mComicRepository.getComicByFolder(folderId).map { list ->
            list.sortedWith(
                compareBy(
                    { extractNumber(it.judul ?: "") },
                    { it.judul?.lowercase() ?: "" }
                )
            )
        }
    }

    private fun extractNumber(name: String): Int {
        val regex = Regex("\\d+")
        val match = regex.find(name)
        return match?.value?.toInt() ?: Int.MAX_VALUE
    }


    fun insert(comic: Comik){
        viewModelScope.launch { mComicRepository.insert(comic) }
    }

    suspend fun getFolderById(idFolder: Int): FolderComik? {
        return mComicRepository.getFolderById(idFolder)
    }
    suspend fun getLastReadInFolder(folderId: Int): Comik? {
        return mComicRepository.getLastReadInFolder(folderId)
    }

    suspend fun updateLastRead(comicId: Int, time: Long) {
        mComicRepository.updateLastRead(comicId, time)
    }

}
