package com.example.zflipcoverkakopreview.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class Room (
    @PrimaryKey(autoGenerate = true)
    val id:Long=0,

    @ColumnInfo(name="room_name")
    val roomName : String?,

    @ColumnInfo(name="recent_chat")
    var recentChat : String?,

    @ColumnInfo(name="recent_dt")
    var recentDt : LocalDateTime?,

    @ColumnInfo(name="new_cnt")
    var newCnt : Int=0,
    //@ColumnInfo(name="room_img")
    //val roomImg : String?,
)