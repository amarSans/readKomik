package com.tugasmobile.readkomik

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.tugasmobile.readkomik.database.ComicRepository
import com.tugasmobile.readkomik.data.Comik
import com.tugasmobile.readkomik.data.FolderComik
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val mComicRepository: ComicRepository =
        ComicRepository(application)
    val displayComics: LiveData<List<Comik>> =
        mComicRepository.getAllComics().map {
            list ->
            list.sortedWith(
                    compareBy(
                        { extractNumber(it.judul ?: "") },
                        { it.judul?.lowercase() ?: "" }
                    )
                )


    }
    val displayFolder : LiveData<List<FolderComik>> = mComicRepository.getAllFolders()

    private fun extractNumber(name: String): Int {
        val regex = Regex("\\d+")
        val match = regex.find(name)
        return match?.value?.toInt() ?: Int.MAX_VALUE
    }


    fun insert(comic: Comik){
        viewModelScope.launch { mComicRepository.insert(comic) }
    }

    fun deleteAll(){
        viewModelScope.launch { mComicRepository.deleteAll() }
    }

    suspend fun insertFolder(folder: FolderComik): Int {
        return mComicRepository.insertFolder(folder)
    }

    suspend fun getFolderByPath(path: String): FolderComik? {
        return mComicRepository.getFolderByPath(path)
    }

    suspend fun getComicByUrl(url: String): Comik? {
        return mComicRepository.getComicByUrl(url)
    }


}
