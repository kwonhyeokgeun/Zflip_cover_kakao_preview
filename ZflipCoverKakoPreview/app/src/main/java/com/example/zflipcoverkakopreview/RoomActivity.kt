package com.example.zflipcoverkakopreview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.zflipcoverkakopreview.adapter.TalkRecyclerViewAdapter
import com.example.zflipcoverkakopreview.databinding.ActivityRoomBinding
import com.example.zflipcoverkakopreview.databinding.ItemTalkBinding
import com.example.zflipcoverkakopreview.db.dao.RoomDao
import com.example.zflipcoverkakopreview.db.dao.TalkDao
import com.example.zflipcoverkakopreview.db.database.AppDatabase
import com.example.zflipcoverkakopreview.db.entity.Talk

class RoomActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRoomBinding
    private lateinit var appDB : AppDatabase
    private lateinit var talkDao : TalkDao
    private lateinit var roomDao : RoomDao
    private lateinit var talkList : ArrayList<Talk>
    private lateinit var adapter : TalkRecyclerViewAdapter
    private var roomId : Long = 0
    private var newCnt : Int = 0
    private var isCreated : Boolean = false
    private lateinit var layoutManager:LinearLayoutManager
    private var screenWidth : Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        roomId = intent.getLongExtra("roomId",0)
        newCnt = intent.getIntExtra("newCnt", 0)
        binding.ibBack.setOnClickListener{
            finish()
        }

        binding.ibBack.bringToFront()
        binding.ibBottom.bringToFront()
        screenWidth = resources.displayMetrics.widthPixels

        talkList = arrayListOf()
        appDB = AppDatabase.getInstance(this)!!
        talkDao = appDB.talkDao()
        roomDao = appDB.roomDao()

        layoutManager = LinearLayoutManager(this)
        getTalkList()



    }
    override fun onStop(){
        //Log.d("카카오 나감",roomId.toString())
        Thread {
            roomDao.setReadById(roomId)
        }.start()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()

//        Handler(Looper.getMainLooper()).postDelayed({
//            Toast.makeText(this, "채팅수 : ${talkList.size}", Toast.LENGTH_SHORT).show()
//        }, 1000)


    }

    private fun setOnClickIbBottom(){
        binding.ibBottom.setOnClickListener{
            layoutManager?.let {
                it.scrollToPosition(talkList.size-1)
            }
        }
    }
    private fun getTalkList(){
        Thread{
            talkList = ArrayList(talkDao.getAllByRoomId(roomId))
            //talkList = ArrayList(talkDao.getAll())
            if(talkList == null ||talkList.size ==0) {
                return@Thread
            }
            setTalkRecyclerView()
            setOnClickIbBottom()
        }.start()
    }

    private fun setTalkRecyclerView(){
        runOnUiThread{
            adapter = TalkRecyclerViewAdapter(talkList, screenWidth)
            binding.rvTalkList.adapter = adapter
            layoutManager.stackFromEnd = true
            binding.rvTalkList.layoutManager = layoutManager

            //처음한번만 새톡으로 이동
            if(!isCreated){
                var newPosition = talkList.size-1 -newCnt
                if(newPosition<0) newPosition = 0
                //Log.d("카카오 새톡", newPosition.toString())
                layoutManager?.let {
                    it.scrollToPositionWithOffset(newPosition,0)
                }
                isCreated = true
            }
        }
    }

}