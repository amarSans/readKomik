package com.tugasmobile.readkomik

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.tugasmobile.readkomik.data.ComicRepository
import com.tugasmobile.readkomik.data.database.Comik
import kotlinx.coroutines.launch

class ComicViewModel(application: Application): AndroidViewModel(application) {
    private val mComicRepository: ComicRepository =
        ComicRepository(application)
    val allComicsdata: LiveData<List<Comik>> =
        mComicRepository.getAllComics()
    val allComicsSorted: LiveData<List<Comik>> =
        allComicsdata.map { list: List<Comik> ->
            list.sortedWith(compareBy { comic ->
                comic.judul
            })
        }




    fun insert(comic: Comik){
        viewModelScope.launch { mComicRepository.insert(comic) }
    }
    fun update(comic: Comik){
        mComicRepository.update(comic)
    }
    fun deleteAll(){
        viewModelScope.launch { mComicRepository.deleteAll() }
    }
    suspend fun getComicById(id: Int): Comik? {
         return mComicRepository.getComicById(id)
    }
    fun updateTotalHalaman(comicID: Int, total: Int){
        viewModelScope.launch {
            mComicRepository.updateTotalHalaman(comicID, total)
        }
    }

    fun updateProgress(comikId: Int, page: Int){
        viewModelScope.launch {
        mComicRepository.updateProgress(comikId, page)
    } }


}
