package com.example.zflipcoverkakopreview.service

import android.app.Notification
import android.app.Person
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.zflipcoverkakopreview.db.dao.RoomDao
import com.example.zflipcoverkakopreview.db.dao.TalkDao
import com.example.zflipcoverkakopreview.db.database.AppDatabase
import com.example.zflipcoverkakopreview.db.entity.Room
import com.example.zflipcoverkakopreview.db.entity.Talk
import com.example.zflipcoverkakopreview.eventbus.NotifyRoomEventBus
import com.example.zflipcoverkakopreview.eventbus.NotifyTalkEventBus
import com.example.zflipcoverkakopreview.eventbus.TestBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception
import java.net.URL
import java.time.LocalDateTime

class MyNotificationListenerService : NotificationListenerService() {
    private lateinit var roomDao : RoomDao
    private lateinit var talkDao : TalkDao
    private lateinit var appDB : AppDatabase
    private var extras : Bundle? = null
    override fun onListenerConnected() {
        super.onListenerConnected()

    }
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val notification = sbn?.notification
        extras = sbn?.notification?.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        val smallIcon = notification?.smallIcon //카톡이미지
        val largeIcon = notification?.getLargeIcon()//방이미지





        // Icon을 Bitmap으로 변환
//        val largeIconBitmap: Bitmap? = largeIcon?.loadDrawable(this)?.toBitmap()
//        val smallIconBitmap: Bitmap? = smallIcon?.loadDrawable(this)?.toBitmap()






        val userName = title
        var roomName = subText.toString()
        var isGroup=false
        if (roomName =="null") {
            roomName = userName.toString()
            isGroup=true
        }

        //val chat = text.toString()
        var chat = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT).toString()
        if(chat=="null") chat = text.toString()
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

        val roomBitmap: Bitmap? = null
        val profileBitmap:Bitmap? = null
        CoroutineScope(Dispatchers.IO).launch {
            var talk : Talk? = null
            appDB.runInTransaction {
                var room = getUpdatedRoom(roomName, chat, now)
                val talkId = addTalk(room.id, userName!!, chat, now)
                talk = talkDao.getByTalkId(talkId)
            }
            NotifyRoomEventBus.notifyRoomChanged()
            talk?.let {
                NotifyTalkEventBus.notifyTalkChanged(it)
            }
        }


        //아이콘 처리
        /*val roomIcon = extras?.get(Notification.EXTRA_LARGE_ICON) as? Icon
        var profileIcon :Icon?=null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
            val messages = notification?.extras?.getParcelableArray(Notification.EXTRA_MESSAGES)
            if (messages != null) {
                val message = messages[0]
                if(message is Bundle &&Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                     val person =message.get("sender_person") as? Person
                     profileIcon = person?.icon
                     CoroutineScope(Dispatchers.IO).launch {
                         profileIcon?.let {
                             TestBus.imageChange(it)
                         }
                     }
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            roomIcon?.let {
                //TestBus.imageChange(it)
            }
        }*/

    }

    private fun getRoomBitmap() : Bitmap?{
        val roomIcon = extras?.get(Notification.EXTRA_LARGE_ICON) as? Icon ?: return null
        return roomIcon.loadDrawable(this)?.toBitmap()
    }

    private fun getProfileBitmap() : Bitmap?{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
            val messages = extras?.getParcelableArray(Notification.EXTRA_MESSAGES)
            if (!messages.isNullOrEmpty()) {
                val message = messages[0]
                if(message is Bundle &&Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                    val person =message.get("sender_person") as? Person
                    val profileIcon = person?.icon
                    return profileIcon?.loadDrawable(this)?.toBitmap()
                }
            }
        }
        return null
    }

    private fun getUpdatedRoom(roomName : String, chat : String, now : LocalDateTime) : Room{
        var room : Room
        room = roomDao.getByRoomName(roomName)

        if(room == null){
            var roomId:Long =0L
            try{
                val roomImg = getRoomBitmap()
                roomId = roomDao.insert(Room(0, roomName, chat, now,1,roomImg, now))
            }catch (e:Exception){
                roomId = roomDao.getByRoomName(roomName).id
            }

            room = roomDao.getByRoomId(roomId)
            //Log.d("카카오 새룸", "${roomId} ${room}") //룸 id확인하기
        }else{
            val roomId = room.id
            //Log.d("카카오 기존룸", "${roomId} ${room}")
            if(room.imgRegDt.isBefore(now.minusDays(1))){
                //이미지 업뎃
                val roomImg = getRoomBitmap()
                roomDao.updateImgById(roomId,chat, room.newCnt+1, now, roomImg)
            }else{
                roomDao.updateById(roomId, chat,room.newCnt+1, now)
            }


        }
        return room
    }

    private fun addTalk(roomId : Long, userName : String, chat : String, now : LocalDateTime):Long{
        val talk = Talk(0, roomId, userName, chat, now)

        return talkDao.insert(talk)
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
        val packNameList = (sbn?.packageName!!).split(".")
        if(packNameList.size<2) return
        val packName = packNameList[1]
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
                NotifyRoomEventBus.notifyRoomChanged()
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        appDB = AppDatabase.getInstance(this)!!
        roomDao = appDB.roomDao()
        talkDao = appDB.talkDao()
        Log.d("카카오 시작","시작")
        CoroutineScope(Dispatchers.IO).launch {
            var room: Room? = roomDao.getByRoomId(1)
            if (room == null) {
                roomDao.insert(Room(1, "시스템", "시작", LocalDateTime.now(), 1, null, LocalDateTime.now()))
            } else {
                roomDao.updateById(1, "시작", 1, LocalDateTime.now())
            }
            talkDao.insert(Talk(0, 1, "시스템", "시작", LocalDateTime.now()))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.IO).launch {
            var room: Room? = roomDao.getByRoomId(1)
            if (room == null) {
                roomDao.insert(Room(1, "시스템", "종료", LocalDateTime.now(), 1,null, LocalDateTime.now()))
            } else {
                roomDao.updateById(1, "종료", 1, LocalDateTime.now())
            }
            talkDao.insert(Talk(0, 1, "시스템", "종료", LocalDateTime.now()))
        }
    }
}