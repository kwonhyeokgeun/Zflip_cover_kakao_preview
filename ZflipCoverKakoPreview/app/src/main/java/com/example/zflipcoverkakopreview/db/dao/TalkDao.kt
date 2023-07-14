package com.example.zflipcoverkakopreview.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.zflipcoverkakopreview.db.entity.Talk
import java.time.LocalDateTime

@Dao
interface TalkDao {
    @Insert
    fun insert(talk : Talk) : Long

    @Query("Select * from talk where id = :talkId")
    fun getByTalkId(talkId : Long) : Talk

    @Query("SELECT * FROM TALK")
    fun getAll():List<Talk>

    @Query("SELECT * FROM TALK WHERE room_id = :roomId and id>:lastId")
    fun getNewTalk(roomId:Long, lastId:Long):List<Talk>

    @Query("SELECT * FROM TALK WHERE room_id = :roomId")
    fun getAllByRoomId(roomId: Long) : List<Talk>

    @Query("DELETE FROM talk WHERE reg_dt <= :daysAgo")
    fun deleteOldTalk(daysAgo: LocalDateTime)
}