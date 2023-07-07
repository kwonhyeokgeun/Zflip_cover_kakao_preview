package com.example.zflipcoverkakopreview.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zflipcoverkakopreview.db.entity.Room
import java.time.LocalDateTime

@Dao
interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(room : Room) : Long

    @Query("SELECT * FROM room WHERE room_name == :roomName limit 1")
    fun getByRoomName(roomName : String) : Room

    @Query("SELECT * FROM room WHERE id == :roomId")
    fun getByRoomId(roomId : Long) : Room

    @Query("Select * from room order by recent_dt DESC")
    fun getAll() : List<Room>

    @Query("UPDATE room SET new_cnt=0 WHERE id = :id")
    fun setReadById(id : Long)

    @Query("UPDATE room SET new_cnt=0 WHERE room_name = :roomName")
    fun setReadByRoomName(roomName : String)

    @Query("UPDATE room SET  recent_chat=:recentChat ,new_cnt =:newCnt, recent_dt=:recentDt where id=:id")
    fun updateById(id : Long, recentChat : String, newCnt : Int, recentDt : LocalDateTime )

    @Query("Delete from room where id = :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM room WHERE recent_dt <= :daysAgo")
    fun deleteOldRoom(daysAgo: LocalDateTime)
}