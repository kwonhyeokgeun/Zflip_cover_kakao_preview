package com.example.zflipcoverkakopreview.db.dao

import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zflipcoverkakopreview.db.entity.Member
import com.example.zflipcoverkakopreview.db.entity.Room
import java.time.LocalDateTime

@Dao
interface MemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(member : Member) : Long

    @Query("SELECT * FROM Member WHERE name=:name")
    fun getByName(name : String):Member?

    @Query("UPDATE Member SET profile_img=:img, img_reg_dt = :now WHERE id=:id")
    fun updateImgById(id : Long, img : Bitmap?, now : LocalDateTime)

    @Query("DELETE FROM Member WHERE img_reg_dt < :daysAgo")
    fun deleteOldMember(daysAgo: LocalDateTime)
}