package com.tugasmobile.readkomik.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tugasmobile.readkomik.data.FolderComik

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderComik):Long

    @Update
    suspend fun update(folder: FolderComik)

    @Delete
    suspend fun delete(folder: FolderComik)

    @Query("SELECT * FROM folders ORDER BY folderName ASC")
    fun getAllFolders(): LiveData<List<FolderComik>>

    @Query("SELECT * FROM folders WHERE folderPath = :path LIMIT 1")
    suspend fun getFolderByPath(path: String): FolderComik?

    @Query("SELECT * FROM folders WHERE idFolder = :idFolder LIMIT 1")
    suspend fun getFolderById(idFolder: Int): FolderComik?
}