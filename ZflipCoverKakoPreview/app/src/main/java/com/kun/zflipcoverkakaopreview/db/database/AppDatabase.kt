package com.kun.zflipcoverkakopreview.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kun.zflipcoverkakopreview.db.converter.BitmapConverter
import com.kun.zflipcoverkakopreview.db.converter.LocalDataTimeConverter
import com.kun.zflipcoverkakopreview.db.dao.MemberDao
import com.kun.zflipcoverkakopreview.db.dao.RoomDao
import com.kun.zflipcoverkakopreview.db.dao.TalkDao
import com.kun.zflipcoverkakopreview.db.entity.Member
import com.kun.zflipcoverkakopreview.db.entity.Room
import com.kun.zflipcoverkakopreview.db.entity.Talk


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
