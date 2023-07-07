package com.example.zflipcoverkakopreview.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Room::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Talk(
    @PrimaryKey(autoGenerate = true)
    val id : Long=0,

    @ColumnInfo(name = "roomId")
    val roomId: Long,

    @ColumnInfo(name = "name")
    val name : String?,

    @ColumnInfo(name = "chat")
    val chat : String?,

    @ColumnInfo(name = "reg_dt")
    val regDt : LocalDateTime
)
