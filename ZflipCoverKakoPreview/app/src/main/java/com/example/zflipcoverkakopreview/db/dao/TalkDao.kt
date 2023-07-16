package com.example.zflipcoverkakopreview.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.zflipcoverkakopreview.db.entity.Talk
import com.example.zflipcoverkakopreview.db.entity.TalkItem
import java.time.LocalDateTime

@Dao
interface TalkDao {
    @Insert
    fun insert(talk : Talk) : Long

    @Query("SELECT Talk.*, Member.name, Member.profile_img FROM Talk INNER JOIN Member ON Talk.member_id = Member.id WHERE Talk.id = :talkId")
    fun getTalkItemByTalkId(talkId : Long) : TalkItem

    @Query("SELECT * FROM TALK")
    fun getAll():List<Talk>

    @Query("SELECT Talk.*, Member.name, Member.profile_img FROM Talk INNER JOIN Member ON Talk.member_id = Member.id WHERE Talk.room_id = :roomId and Talk.id>:lastId")
    fun getNewTalkItem(roomId:Long, lastId:Long):List<TalkItem>

    @Query("SELECT * FROM TALK WHERE room_id = :roomId")
    fun getAllByRoomId(roomId: Long) : List<Talk>

    @Query("DELETE FROM talk WHERE reg_dt <= :daysAgo")
    fun deleteOldTalk(daysAgo: LocalDateTime)

    @Query("SELECT Talk.*, Member.name, Member.profile_img FROM Talk INNER JOIN Member ON Talk.member_id = Member.id WHERE Talk.room_id = :roomId")
    fun getTalkItemsByRoomId(roomId : Long) : List<TalkItem>
}