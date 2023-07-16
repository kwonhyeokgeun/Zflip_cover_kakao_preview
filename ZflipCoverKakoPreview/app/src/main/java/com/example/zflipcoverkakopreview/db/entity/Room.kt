package com.example.zflipcoverkakopreview.db.entity

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(indices = [Index(value = ["room_name"], unique = true)])
data class Room (
    @PrimaryKey(autoGenerate = true)
    val id:Long=0,

    @ColumnInfo(name="room_name")
    val roomName : String?,

    @ColumnInfo(name="recent_chat")
    var recentChat : String?,

    @ColumnInfo(name="recent_dt")
    var recentDt : LocalDateTime,

    @ColumnInfo(name="new_cnt")
    var newCnt : Int=0,

    @ColumnInfo(name="room_img")
    val roomImg : Bitmap?,

    @ColumnInfo(name="img_reg_dt")
    var imgRegDt : LocalDateTime,
)