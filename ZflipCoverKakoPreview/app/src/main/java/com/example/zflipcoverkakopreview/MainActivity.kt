package com.example.zflipcoverkakopreview

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zflipcoverkakopreview.adapter.OnRoomClickListener
import com.example.zflipcoverkakopreview.adapter.RoomRecyclerViewAdapter
import com.example.zflipcoverkakopreview.databinding.ActivityMainBinding
import com.example.zflipcoverkakopreview.db.dao.RoomDao
import com.example.zflipcoverkakopreview.db.dao.TalkDao
import com.example.zflipcoverkakopreview.db.database.AppDatabase
import com.example.zflipcoverkakopreview.db.entity.Room
import com.example.zflipcoverkakopreview.eventbus.NotifyRoomEventBus
import com.example.zflipcoverkakopreview.eventbus.TestBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() , OnRoomClickListener{
    private lateinit var binding : ActivityMainBinding
    private lateinit var appDB : AppDatabase
    private lateinit var roomDao : RoomDao
    private lateinit var talkDao : TalkDao
    private lateinit var roomList : ArrayList<Room>
    private lateinit var adapter : RoomRecyclerViewAdapter
    private lateinit var scope : CoroutineScope
    private val eventBus = NotifyRoomEventBus
    private val eventBusTest = TestBus
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //binding 객체 받기
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appDB = AppDatabase.getInstance(this)!!
        roomDao = appDB.roomDao()
        talkDao = appDB.talkDao()

        if (!permissionGrantred()) {
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
        }.start()
    }

    private fun permissionGrantred(): Boolean {
        val sets = NotificationManagerCompat.getEnabledListenerPackages(this)
        return sets != null && sets.contains(packageName)
    }

    override fun onResume() {
        super.onResume()

        scope = MainScope()
        scope.launch { //이벤트버스 구독 등록
            eventBus.notifyEvents.collect {
                updateRoomRecyclerView() //채팅방 업데이트
            }

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