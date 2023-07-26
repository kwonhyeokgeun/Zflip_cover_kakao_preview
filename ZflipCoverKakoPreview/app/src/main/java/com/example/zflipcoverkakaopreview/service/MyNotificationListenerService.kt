package com.example.zflipcoverkakopreview.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Person
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.zflipcoverkakopreview.R
import com.example.zflipcoverkakopreview.db.dao.MemberDao
import com.example.zflipcoverkakopreview.db.dao.RoomDao
import com.example.zflipcoverkakopreview.db.dao.TalkDao
import com.example.zflipcoverkakopreview.db.database.AppDatabase
import com.example.zflipcoverkakopreview.db.entity.Member
import com.example.zflipcoverkakopreview.db.entity.Room
import com.example.zflipcoverkakopreview.db.entity.Talk
import com.example.zflipcoverkakopreview.db.entity.TalkItem
import com.example.zflipcoverkakopreview.eventbus.EventBusB
import com.example.zflipcoverkakopreview.eventbus.NotifyRoomEventBus
import com.example.zflipcoverkakopreview.eventbus.NotifyTalkEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class MyNotificationListenerService : NotificationListenerService() {
    private lateinit var roomDao : RoomDao
    private lateinit var talkDao : TalkDao
    private lateinit var memberDao : MemberDao
    private lateinit var appDB : AppDatabase
    private var extras : Bundle? = null
    private var isGroup:Boolean = false

    private val this_=this
    val SUMMARY_ID = 0
    val GROUP_KEY = "com.example.zflipkakaopreview"
    val CHANNEL_ID="asdfasdf"
    val CHANNEL_NAME = "dfdfdf"
    var notifiPermission = false


    override fun onListenerConnected() {
        super.onListenerConnected()
    }


    @SuppressLint("RestrictedApi")
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val notification = sbn?.notification
        extras = sbn?.notification?.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        //val smallIcon = notification?.smallIcon //카톡이미지
        //val largeIcon = notification?.getLargeIcon()//방이미지


        val userName = title?.trim()
        var roomName = subText.toString().trim()

        val packNameList = (sbn?.packageName!!).split(".")
        if (packNameList.size<2) return
        val packName = packNameList[1]

        /*if(packName =="example"){
            Log.d("카카오 사진임티", extras?.get(Notification.EXTRA_PICTURE).toString())
            val bitmap = extras?.get(Notification.EXTRA_PICTURE) as? Bitmap
            CoroutineScope(Dispatchers.IO).launch {
                bitmap?.let {
                    EventBusB.notifyTalkChanged(it)
                }
            }
        }*/

        if(packName != "kakao" || sbn?.id!=2)
            return

        isGroup=true
        if (roomName =="null") {
            roomName = userName.toString()
            isGroup=false
        }

        val chat = text.toString()
        /*var chat = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT).toString()
        if(chat=="null") chat = text.toString()*/

        //val user = MessagingStyle.extractMessagingStyleFromNotification(notification)?.user

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && notification?.hasImage() == true) { //이미지 있음

            CoroutineScope(Dispatchers.IO).launch {
                bm?.let {
                    EventBusB.notifyTalkChanged(it)
                }
            }

        }*/
        val now = LocalDateTime.now()

        //새톡 저장 및 이벤트 전송
        CoroutineScope(Dispatchers.IO).launch {
            var talkItem : TalkItem? = null
            var room : Room? = null
            appDB.runInTransaction {
                room = getUpdatedRoom(roomName, chat, now)
                val talkId = addTalk(sbn, room!!.id, userName!!, chat, now)
                talkItem = talkDao.getTalkItemByTalkId(talkId)
            }
            NotifyRoomEventBus.notifyRoomChanged()
            talkItem?.let {
                NotifyTalkEventBus.notifyTalkChanged(it)
            }
            if(notifiPermission ){
                //알림킨 방이면 조건 추가하기

                addNoti(sbn, talkItem!!.id, room!!.roomName!!, talkItem!!.name!!, talkItem!!.chat!!)
            }

        }

    }


    private fun getUpdatedRoom(roomName : String, chat : String, now : LocalDateTime) : Room{
        var room : Room
        room = roomDao.getByRoomName(roomName)

        if(room == null){
            //Log.d("카카오 새룸", "${roomId} ${room}")
            var roomId:Long =0L
            try{
                val roomImg = getRoomBitmap()
                roomId = roomDao.insert(Room(0, roomName, chat, now,1,roomImg, now))
            }catch (e:Exception){
                roomId = roomDao.getByRoomName(roomName).id
            }

            room = roomDao.getByRoomId(roomId)

        }else{
            //Log.d("카카오 기존룸", "${roomId} ${room}")
            val roomId = room.id
            if(room.roomImg==null || room.imgRegDt.isBefore(now.minusDays(1))){
                //이미지 업뎃
                val roomImg = getRoomBitmap()
                roomDao.updateImgById(roomId,chat, room.newCnt+1, now, roomImg)
            }else{
                roomDao.updateById(roomId, chat,room.newCnt+1, now)
            }
        }
        return room
    }

    private fun addTalk(sbn: StatusBarNotification?,roomId : Long, memberName : String, chat : String, now : LocalDateTime):Long{
        val memberId = getMemberId(sbn, memberName)
        val talk = Talk(0, roomId, memberId, chat, now)

        return talkDao.insert(talk)
    }

    private fun getMemberId(sbn: StatusBarNotification?, memberName: String) : Long{
        val member =  memberDao.getByName(memberName)
        if(member==null){ //첫맴버
            val profileImg = getProfileBitmap(sbn)
            return memberDao.insert(Member(0,memberName, profileImg, LocalDateTime.now()))
        }
        else{ //기존맴버
            if(member.profileImg==null || member.imgRegDt.isBefore(LocalDateTime.now().minusDays(1))){
                //프사 업데이트
                val profileImg = getProfileBitmap(sbn)
                memberDao.updateImgById(member.id, profileImg, LocalDateTime.now())
            }
            return member.id
        }
    }



    private fun getRoomBitmap() : Bitmap?{
        val roomIcon = extras?.get(Notification.EXTRA_LARGE_ICON) as? Icon

        if(roomIcon==null){
            Log.d("카카오 방 사진 널", "널")
            return null
        }
        return roomIcon.loadDrawable(this)?.toBitmap()
    }

    private fun getProfileBitmap(sbn: StatusBarNotification?) : Bitmap?{
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val messages = sbn?.notification?.extras?.getParcelableArray(Notification.EXTRA_MESSAGES)
                if (!messages.isNullOrEmpty()) {
                    val message = messages[0]
                    if (message is Bundle && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val person = message.get("sender_person") as? Person
                        val profileIcon = person?.icon
                        return profileIcon?.loadDrawable(this)?.toBitmap()
                    }
                }
            }
        }catch (e : Exception){
            Log.d("카카오 프사에러", e.toString())
            return null
        }
        return null
    }
    private fun getProfileIcon(sbn: StatusBarNotification?) : Icon?{
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val messages = sbn?.notification?.extras?.getParcelableArray(Notification.EXTRA_MESSAGES)
                if (!messages.isNullOrEmpty()) {
                    val message = messages[0]
                    if (message is Bundle && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val person = message.get("sender_person") as? Person
                        val profileIcon = person?.icon
                        return profileIcon
                    }
                }
            }
        }catch (e : Exception){
            Log.d("카카오 프사에러", e.toString())
            return null
        }
        return null
    }


    override fun onNotificationRemoved(sbn: StatusBarNotification?, rankingMap: RankingMap?) {
        super.onNotificationRemoved(sbn, rankingMap)
        val notification = sbn?.notification
        val extras = sbn?.notification?.extras
        val title = extras?.getString(Notification.EXTRA_TITLE)?.toString()
        //val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        //val smallIcon = notification?.smallIcon
        //val largeIcon = notification?.getLargeIcon()
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
        if(packName != "kakao" || sbn?.id!=2)
            return

        var roomName = subText.toString()
        var isGroup=false
        if (roomName =="null") {
            val userName = title.toString()
            roomName = userName.toString()
            isGroup=true
        }

        //알림 읽음처리
        CoroutineScope(Dispatchers.IO).launch {
            roomDao.setReadByRoomName(roomName)
            NotifyRoomEventBus.notifyRoomChanged()
        }


    }

    override fun onCreate() {
        super.onCreate()
        appDB = AppDatabase.getInstance(this)!!
        roomDao = appDB.roomDao()
        talkDao = appDB.talkDao()
        memberDao = appDB.memberDao()
        if (ActivityCompat.checkSelfPermission(this_, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            notifiPermission = true
            // 알림 채널 생성
            createNotificationChannel(this, CHANNEL_ID, CHANNEL_NAME)
            //makeNoti()
        }


    }

    fun createNotificationChannel(context: Context, channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            // 진동 패턴을 설정하지 않도록 합니다.
            channel.vibrationPattern = null
            notificationManager.createNotificationChannel(channel)
        }
    }
    fun makeNoti(){
        val largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
        /*val newMessageNotification1 = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.profile)
            .setContentTitle("email 1 title")
            .setContentText("email 1 text")
            .setGroup(GROUP_KEY)
            .build()

        val newMessageNotification2 = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.profile)
            .setContentTitle("email 2 title")
            .setContentText("email 2 text")
            .setGroup(GROUP_KEY)
            .build()*/



        val summaryNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("요약 title")
            .setContentText("요약 text")
            .setSmallIcon(R.drawable.baseline_wysiwyg_24)
            .setStyle(
                NotificationCompat.InboxStyle()
                //.setBigContentTitle("빅 타이틀")
                .setSummaryText(""))
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .build()

        NotificationManagerCompat.from(this).apply {
            if (ActivityCompat.checkSelfPermission(
                    this_,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            notify(SUMMARY_ID, summaryNotification)
        }
    }

    fun addNoti(sbn: StatusBarNotification?, talkId : Long, roomName : String, userName : String, chat : String){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifications = notificationManager.getActiveNotifications()
        var haveNoti = false
        for(a in notifications){
            val lst = a.groupKey.split("|")
            if(lst.size>1){
                val groupKey = lst[1]
                if(groupKey=="com.example.zflipcoverkakopreview"){
                    haveNoti=true
                    break
                }
            }
        }



        if(!haveNoti){ //처음이면
            val summaryNotification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("요약 title")
                .setContentText("요약 text")
                .setSmallIcon(R.drawable.baseline_wysiwyg_24)
                .setStyle(
                    NotificationCompat.InboxStyle()
                        //.setBigContentTitle("빅 타이틀")
                        .setSummaryText(""))
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .build()
            val defMessageNotification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.profile)
                .setContentTitle("")
                .setContentText("")
                .setGroup(GROUP_KEY)
                .build()

            NotificationManagerCompat.from(this).apply {
                if (ActivityCompat.checkSelfPermission(
                        this_,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }

                notify(SUMMARY_ID, summaryNotification)
                notify(System.currentTimeMillis().toInt()-1, defMessageNotification)


            }
        }

        var title =if(roomName==userName)
                            userName
                        else {
                            val roomShortName = if(roomName.length>7) roomName.substring(0,5)+".." else roomName
                            "${userName}[${roomShortName}]"
                        }
        val profileBitmap=getProfileBitmap(sbn)
        if(profileBitmap!=null){
            val profileIcon = IconCompat.createWithAdaptiveBitmap(profileBitmap)
            val newMessageNotification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(profileIcon)
                .setContentTitle(title)
                .setContentText(chat)
                .setGroup(GROUP_KEY)
                .build()
            val this_ = this
            NotificationManagerCompat.from(this).apply {
                if (ActivityCompat.checkSelfPermission(
                        this_,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }

                notify(talkId.toInt(), newMessageNotification)
            }
        }

    }

}