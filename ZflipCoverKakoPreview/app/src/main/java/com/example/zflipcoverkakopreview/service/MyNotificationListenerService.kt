package com.example.zflipcoverkakopreview.service

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.zflipcoverkakopreview.db.dao.RoomDao
import com.example.zflipcoverkakopreview.db.dao.TalkDao
import com.example.zflipcoverkakopreview.db.database.AppDatabase
import com.example.zflipcoverkakopreview.db.entity.Room
import com.example.zflipcoverkakopreview.db.entity.Talk
import com.example.zflipcoverkakopreview.eventbus.NotifyEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.time.LocalDateTime

class MyNotificationListenerService : NotificationListenerService() {
    private lateinit var roomDao : RoomDao
    private lateinit var talkDao : TalkDao
    private lateinit var appDB : AppDatabase
    override fun onListenerConnected() {
        super.onListenerConnected()
        appDB = AppDatabase.getInstance(this)!!
        roomDao = appDB.roomDao()
        talkDao = appDB.talkDao()
    }
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val notification = sbn?.notification
        val extras = sbn?.notification?.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        val smallIcon = notification?.smallIcon
        val largeIcon = notification?.getLargeIcon()

        /*
        // Icon을 Bitmap으로 변환
        //val largeIconBitmap: Bitmap? = largeIcon?.loadDrawable(this)?.toBitmap()
        //imageView.setImageBitmap(largeIconBitmap


        val mainActivity = applicationContext as MainActivity
        val imageView: ImageView = mainActivity.findViewById(R.id.imageView)
        val drawable = largeIcon?.loadDrawable(mainActivity)
        imageView.setImageDrawable(drawable) //스레드에러남
        */




        val userName = title
        var roomName = subText.toString()
        var isGroup=false
        if (roomName =="null") {
            roomName = userName.toString()
            isGroup=true
        }

        val chat = text.toString()
        val now = LocalDateTime.now()

        /*Log.d("카카오 푸쉬 Log","onNotificationPosted ~ " +
                " packageName: " + sbn?.packageName +
                " id: " + sbn?.id +
                " postTime: " + sbn?.postTime +
                " title: " + title +  //name
                " text : " + text +  //chat
                " subText: " + subText) //roomName*/
        val packNameList = (sbn?.packageName!!).split(".")
        if (packNameList.size<2) return
        val packName = packNameList[1]

        if(packName != "kakao" || sbn?.id!=2)
            return
        CoroutineScope(Dispatchers.IO).launch {
            appDB.runInTransaction {
                var room = getUpdatedRoom(roomName, chat, now)
                addTalk(room.id, userName!!, chat, now)
            }
            NotifyEventBus.notifyTalkChanged()
        }

    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun getUpdatedRoom(roomName : String, chat : String, now : LocalDateTime) : Room{
        var room : Room
        room = roomDao.getByRoomName(roomName)

        if(room == null){
            var roomId:Long =0L
            try{
                roomId = roomDao.insert(Room(0, roomName, chat, now,1))
            }catch (e:Exception){
                roomId = roomDao.getByRoomName(roomName).id
            }

            room = roomDao.getByRoomId(roomId)
            //Log.d("카카오 새룸", "${roomId} ${room}") //룸 id확인하기
        }else{
            val roomId = room.id
            //Log.d("카카오 기존룸", "${roomId} ${room}")
            roomDao.updateById(room.id, chat,room.newCnt+1, now)

        }
        return room
    }

    private fun addTalk(roomId : Long, userName : String, chat : String, now : LocalDateTime){
        val talk = Talk(0, roomId, userName, chat, now)

        talkDao.insert(talk)
        //val talkList2 = talkDao.getAllByRoomId(roomId)

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?) {
        super.onNotificationRemoved(sbn, rankingMap)
        val notification = sbn?.notification
        val extras = sbn?.notification?.extras
        val title = extras?.getString(Notification.EXTRA_TITLE)?.toString()
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val smallIcon = notification?.smallIcon
        val largeIcon = notification?.getLargeIcon()
        /*Log.d("카카오 제거 Log","onNotificationPosted ~ " +
                " packageName: " + sbn?.packageName +
                " id: " + sbn?.id +
                " postTime: " + sbn?.postTime +
                " title: " + title +  //name
                " text : " + text +  //chat
                " subText: " + subText) //roomName*/
        val packName = (sbn?.packageName!!).split(".")[1]
        var roomName = subText.toString()
        var isGroup=false
        if (roomName =="null") {
            val userName = title.toString()
            roomName = userName.toString()
            isGroup=true
        }
        if(packName == "kakao" && sbn?.id==2){
            CoroutineScope(Dispatchers.IO).launch {
                roomDao.setReadByRoomName(roomName)
                NotifyEventBus.notifyTalkChanged()
            }
        }

    }
}