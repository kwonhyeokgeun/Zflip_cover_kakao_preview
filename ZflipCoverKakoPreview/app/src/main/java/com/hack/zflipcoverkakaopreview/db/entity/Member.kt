package com.example.zflipcoverkakopreview.db.entity

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Member(
    @PrimaryKey(autoGenerate = true)
    val id:Long=0,

    @ColumnInfo(name="name")
    val name : String?,

    @ColumnInfo(name="profile_img")
    val profileImg : Bitmap?,

    @ColumnInfo(name="img_reg_dt")
    val imgRegDt : LocalDateTime,
)
