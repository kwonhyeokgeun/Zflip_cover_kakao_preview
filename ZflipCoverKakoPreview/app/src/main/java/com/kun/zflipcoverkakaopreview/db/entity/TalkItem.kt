package com.kun.zflipcoverkakopreview.db.entity

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import java.time.LocalDateTime

data class TalkItem(
    val id : Long=0,

    @ColumnInfo("room_id")
    val roomId: Long,

    @ColumnInfo("member_id")
    val memberId : Long?,

    val chat : String?,
    @ColumnInfo("reg_dt")
    val regDt : LocalDateTime,

    val name : String?,

    @ColumnInfo("profile_img")
    val profileImg : Bitmap?
)
