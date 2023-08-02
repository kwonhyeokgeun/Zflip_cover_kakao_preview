package com.kun.zflipcoverkakopreview.db.entity

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
            childColumns = ["room_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["member_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Talk(
    @PrimaryKey(autoGenerate = true)
    val id : Long=0,

    @ColumnInfo(name = "room_id")
    val roomId: Long,

    @ColumnInfo(name = "member_id")
    val memberId : Long?,

    @ColumnInfo(name = "chat")
    val chat : String?,

    @ColumnInfo(name = "reg_dt")
    val regDt : LocalDateTime
)
