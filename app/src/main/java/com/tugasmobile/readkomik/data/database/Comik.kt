package com.tugasmobile.readkomik.data.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "comik")
@Parcelize
data class Comik(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id:Int=0,
    @ColumnInfo(name = "pdfUrl")
    var pdfUrl:String?=null,

    @ColumnInfo(name = "judul")
    var judul: String?=null,

    @ColumnInfo(name = "progress")
    var progress:Int=0,

    @ColumnInfo(name = "totalHalaman")
    var totalHalaman:Int=0,

    @ColumnInfo(name= "gambar")
    var gambar:String?=null,
    ): Parcelable