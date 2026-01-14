package com.tugasmobile.readkomik.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ComicDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(comic: Comik)

    @Query("DELETE FROM comik")
    suspend fun deleteAll()

    @Update
    fun update(comic: Comik)

    @Query("SELECT * FROM comik ORDER BY judul ASC")
    fun getAllComics(): LiveData<List<Comik>>

    @Query("UPDATE Comik SET progress = :page WHERE id = :comicId")
    suspend fun updateProgress(comicId: Int, page: Int)

    @Query("UPDATE Comik SET totalHalaman = :total WHERE id = :comicID")
    suspend fun updateTotalHalaman(comicID: Int, total: Int)

    @Query("SELECT * FROM comik WHERE id = :id LIMIT 1")
    suspend fun getComicById(id: Int): Comik?




}