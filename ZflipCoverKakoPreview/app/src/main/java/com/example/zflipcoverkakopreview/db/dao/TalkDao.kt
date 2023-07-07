package com.example.zflipcoverkakopreview.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.zflipcoverkakopreview.db.entity.Talk
import java.time.LocalDateTime

@Dao
interface TalkDao {
    @Insert
    fun insert(talk : Talk)

    //@Query("Select * from talk where roomId = :roomId order by id desc limit 1")
    //fun getRecentByRoomId(roomId : Long) : Talk  //왜만들었지?

    @Query("SELECT * FROM TALK")
    fun getAll():List<Talk>

    @Query("SELECT * FROM TALK WHERE roomId = :roomId")
    fun getAllByRoomId(roomId: Long) : List<Talk>

    @Query("DELETE FROM talk WHERE reg_dt <= :daysAgo")
    fun deleteOldTalk(daysAgo: LocalDateTime)
}