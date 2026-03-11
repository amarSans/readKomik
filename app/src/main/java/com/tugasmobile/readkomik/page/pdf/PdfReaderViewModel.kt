package com.tugasmobile.readkomik.page.pdf

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.tugasmobile.readkomik.database.ComicRepository
import com.tugasmobile.readkomik.data.Comik
import kotlinx.coroutines.launch

class PdfReaderViewModel(application: Application): AndroidViewModel(application) {
    private val comicrepository: ComicRepository =
        ComicRepository(application)

    fun getComicsByFolder(folderId: Int): LiveData<List<Comik>> {
        return comicrepository.getComicByFolder(folderId).map { list ->
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
        viewModelScope.launch { comicrepository.insert(comic) }
    }
    fun update(comic: Comik){
        comicrepository.update(comic)
    }
    fun deleteAll(){
        viewModelScope.launch { comicrepository.deleteAll() }
    }
    suspend fun getComicById(id: Int): Comik? {
        return comicrepository.getComicById(id)
    }
    fun updateTotalHalaman(comicID: Int, total: Int){
        viewModelScope.launch {
            comicrepository.updateTotalHalaman(comicID, total)
        }
    }

    fun updateProgress(comikId: Int, page: Int){
        viewModelScope.launch {
            comicrepository.updateProgress(comikId, page)
        } }
}