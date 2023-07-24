package com.example.zflipcoverkakopreview

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zflipcoverkakopreview.adapter.OnRoomClickListener
import com.example.zflipcoverkakopreview.adapter.RoomRecyclerViewAdapter
import com.example.zflipcoverkakopreview.databinding.ActivityMainBinding
import com.example.zflipcoverkakopreview.db.dao.MemberDao
import com.example.zflipcoverkakopreview.db.dao.RoomDao
import com.example.zflipcoverkakopreview.db.dao.TalkDao
import com.example.zflipcoverkakopreview.db.database.AppDatabase
import com.example.zflipcoverkakopreview.db.entity.Member
import com.example.zflipcoverkakopreview.db.entity.Room
import com.example.zflipcoverkakopreview.db.entity.Talk
import com.example.zflipcoverkakopreview.eventbus.EventBusB
import com.example.zflipcoverkakopreview.eventbus.NotifyRoomEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() , OnRoomClickListener{
    private lateinit var binding : ActivityMainBinding
    private lateinit var appDB : AppDatabase
    private lateinit var roomDao : RoomDao
    private lateinit var talkDao : TalkDao
    private lateinit var memberDao : MemberDao
    private lateinit var roomList : ArrayList<Room>
    private lateinit var adapter : RoomRecyclerViewAdapter
    private lateinit var scope : CoroutineScope
    private val eventBus = NotifyRoomEventBus
    private val testBus = EventBusB
    private val this_ = this

    // 알림 채널을 생성하는 함수
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

    fun createNotification(context: Context, imageTitle: String, imageDescription: String, myBitmap: Bitmap) {
        val CHANNEL_ID = "your_channel_id" // 알림 채널 ID를 지정합니다. 고유한 값으로 변경해야 합니다.
        val notificationId = 11232132 // 알림 ID를 지정합니다. 고유한 값으로 변경해야 합니다.

        // 알림 채널 생성
        createNotificationChannel(context, CHANNEL_ID, "Your Channel Name")


        // 알림을 생성하는 코드
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.profile)
            .setContentTitle(imageTitle)
            .setContentText(imageDescription)
            .setLargeIcon(myBitmap)
            .setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(myBitmap)
                .bigLargeIcon(null))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //binding 객체 받기
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appDB = AppDatabase.getInstance(this)!!
        roomDao = appDB.roomDao()
        talkDao = appDB.talkDao()
        memberDao = appDB.memberDao()

        if (!permissionGrantred()) {
            insertInfo()
            val intent = Intent(
                "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        }

        setRoomRecyclerView()
        deleteOldTalk()

    }

    private fun deleteOldTalk(){
        Thread{
            val twoDaysAgo = LocalDateTime.now().minusDays(2)
            talkDao.deleteOldTalk(twoDaysAgo)
            roomDao.deleteOldRoom(twoDaysAgo)
            memberDao.deleteOldMember(twoDaysAgo)
        }.start()
    }

    private fun permissionGrantred(): Boolean {
        val sets = NotificationManagerCompat.getEnabledListenerPackages(this)
        return sets != null && sets.contains(packageName)
    }

    private fun insertInfo(){
        CoroutineScope(Dispatchers.IO).launch {
            appDB.runInTransaction {
                // 트랜잭션 처리 코드
                roomDao.insert(Room(1, getString(R.string.app_info_room), getString(R.string.app_info3), LocalDateTime.now(), 3, null, LocalDateTime.now()))
                memberDao.insert(Member(1, getString(R.string.app_info_room), null, LocalDateTime.now()))
                talkDao.insert(Talk(0, 1, 1L, getString(R.string.app_info1), LocalDateTime.now()))
                talkDao.insert(Talk(0, 1, 1L, getString(R.string.app_info2), LocalDateTime.now()))
                talkDao.insert(Talk(0, 1, 1L, getString(R.string.app_info3), LocalDateTime.now()))
            }
        }

    }

    override fun onResume() {
        super.onResume()

        Log.d("카카오 알림","생성")
        Thread {
            val imageTitle = "title"
            val imageDescription = "text"
            val myBitmap =
                BitmapFactory.decodeResource(resources, R.drawable.logo) // 이미지 리소스를 불러옵니다.
            createNotification(this, imageTitle, imageDescription, myBitmap)
        }.start()

        scope = MainScope()
        scope.launch { //이벤트버스 구독 등록
            eventBus.notifyEvents.collect {
                updateRoomRecyclerView() //채팅방 업데이트
            }
            /*testBus.notifyEvents.collect{ //비트맵 테스트용
                binding.ivTest.setImageBitmap(it)

            }*/

        }


        Handler(Looper.getMainLooper()).postDelayed({
            updateRoomRecyclerView()
        }, 500)
        //Toast.makeText(this, "리쥬매",Toast.LENGTH_SHORT).show()
    }



    override fun onPause() {

        super.onPause()
        scope.cancel() //이벤트버스 구독 취소

    }


    private fun setRoomRecyclerView(){
        roomList = ArrayList()
        runOnUiThread {
            adapter = RoomRecyclerViewAdapter(roomList, this)
            binding.rvRoomList.adapter = adapter
            binding.rvRoomList.layoutManager = LinearLayoutManager(this)
        }
    }

    private fun updateRoomRecyclerView(){
        Thread {
            roomList.clear()
            roomList.addAll(roomDao.getAll())
            runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        }.start()
    }

    override fun onClick(id : Long, newCnt : Int) {
        val intent = Intent(this, RoomActivity::class.java)
        intent.putExtra("roomId",id)
        intent.putExtra("newCnt", newCnt)
        startActivity(intent)
    }

    override fun onLongClick(id: Long) {
        val builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("방 나가기")
        builder.setMessage("나가시겠습니까?")
        builder.setPositiveButton("취소", null)
        builder.setNegativeButton("네", object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                deleteRoom(id)
            }
        })
        builder.show()
    }

    fun deleteRoom(id : Long){
        Thread{
            for(i in 0 until roomList.size){
                if(roomList[i].id == id){
                    roomList.removeAt(i)
                    break
                }
            }
            roomDao.deleteById(id)
            runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        }.start()
    }
}