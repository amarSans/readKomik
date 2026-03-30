package com.tugasmobile.readkomik.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderComik(
    @PrimaryKey(autoGenerate = true)
    val idFolder: Int = 0,
    val folderName: String,
    val folderPath: String,
    val totalPdf: Int,
)
