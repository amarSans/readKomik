package com.tugasmobile.readkomik.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "comik",
    foreignKeys = [
        ForeignKey(
            entity = FolderComik::class,
            parentColumns = ["idFolder"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
)
    ],
    indices = [Index("folderId"),
        Index(value = ["folderId","pdfUrl"], unique = true)]
    )

@Parcelize
data class Comik(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id:Int=0,

    var folderId: Int,

    @ColumnInfo(name = "pdfUrl")
    var pdfUrl:String?=null,

    @ColumnInfo(name = "judul")
    var judul: String?=null,

    @ColumnInfo(name = "progress")
    var progress:Int=0,

    @ColumnInfo(name = "totalHalaman")
    var totalHalaman:Int=0,

    ): Parcelable