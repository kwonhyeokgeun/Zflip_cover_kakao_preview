package com.example.zflipcoverkakopreview.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.zflipcoverkakopreview.db.converter.BitmapConverter
import com.example.zflipcoverkakopreview.db.converter.LocalDataTimeConverter
import com.example.zflipcoverkakopreview.db.dao.MemberDao
import com.example.zflipcoverkakopreview.db.dao.RoomDao
import com.example.zflipcoverkakopreview.db.dao.TalkDao
import com.example.zflipcoverkakopreview.db.entity.Member
import com.example.zflipcoverkakopreview.db.entity.Room
import com.example.zflipcoverkakopreview.db.entity.Talk


@Database(entities = [Talk::class, Room::class, Member::class], version = 1)
@TypeConverters(LocalDataTimeConverter::class, BitmapConverter::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun talkDao() : TalkDao
    abstract fun roomDao() : RoomDao
    abstract fun memberDao() : MemberDao

    companion object{
        val db_name = "talk_privew"
        var appDatabase : AppDatabase? = null

        @Synchronized
        fun getInstance(context : Context) : AppDatabase?{
            if(appDatabase == null){
                synchronized(AppDatabase::class){
                    appDatabase = androidx.room.Room.databaseBuilder(context,
                    AppDatabase::class.java,
                    db_name
                    ).build()
                }
            }

            return appDatabase
        }
    }
}
