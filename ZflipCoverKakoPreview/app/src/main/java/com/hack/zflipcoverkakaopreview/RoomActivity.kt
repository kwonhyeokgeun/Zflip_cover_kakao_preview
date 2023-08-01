package com.hack.zflipcoverkakopreview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hack.zflipcoverkakopreview.adapter.TalkRecyclerViewAdapter
import com.hack.zflipcoverkakopreview.databinding.ActivityRoomBinding
import com.hack.zflipcoverkakopreview.db.dao.RoomDao
import com.hack.zflipcoverkakopreview.db.dao.TalkDao
import com.hack.zflipcoverkakopreview.db.database.AppDatabase
import com.hack.zflipcoverkakopreview.db.entity.TalkItem
import com.hack.zflipcoverkakopreview.eventbus.NotifyTalkEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RoomActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRoomBinding
    private lateinit var appDB : AppDatabase
    private lateinit var talkDao : TalkDao
    private lateinit var roomDao : RoomDao
    private lateinit var talkItemList : ArrayList<TalkItem>
    private lateinit var adapter : TalkRecyclerViewAdapter
    private var roomId : Long = 0
    private var newCnt : Int = 0
    private var isCreated : Boolean = false
    private lateinit var layoutManager:LinearLayoutManager
    private var screenWidth : Int=0
    private lateinit var scope : CoroutineScope
    private val eventBus = NotifyTalkEventBus
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

        talkItemList = arrayListOf()
        appDB = AppDatabase.getInstance(this)!!
        talkDao = appDB.talkDao()
        roomDao = appDB.roomDao()

        layoutManager = LinearLayoutManager(this)
        getTalkList()

    }
    override fun onStop(){
        //톡 읽음 처리
        Thread {
            roomDao.setReadById(roomId)
        }.start()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        scope = MainScope()
        scope.launch { //이벤트버스 구독 등록
            eventBus.notifyEvents.collect {
                if(it.roomId ==roomId){
                    val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                    val itemCount = adapter.itemCount
                    talkItemList.add(it)
                    adapter.notifyDataSetChanged()
                    if(itemCount-1 == lastVisibleItemPosition) {//스크롤이 아래면
                        layoutManager.scrollToPositionWithOffset(itemCount,0)
                    }else{
                        binding.tvNew.visibility = View.VISIBLE
                    }
                }
            }
        }

        //그새 새로온 톡 추가
        updateTalkList()

    }

    private fun updateTalkList(){
        if(talkItemList.size<=0) return
        Thread{
            val lastId = talkItemList[talkItemList.size-1].id
            val newTalkItemList = talkDao.getNewTalkItem(roomId, lastId)
            if(newTalkItemList.size>0){ //새톡있으면
                talkItemList.addAll(newTalkItemList)
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                    binding.tvNew.visibility = View.VISIBLE
                }
            }
        }.start()

    }

    override fun onPause() {
        super.onPause()
        scope.cancel() //이벤트버스 구독 취소
    }

    private fun setOnClickIbBottom(){
        binding.ibBottom.setOnClickListener{
            layoutManager?.let {
                it.scrollToPosition(talkItemList.size-1)
            }
        }
    }
    private fun getTalkList(){
        Thread{
            talkItemList = ArrayList(talkDao.getTalkItemsByRoomId(roomId))
            if(talkItemList == null ||talkItemList.size ==0) {
                return@Thread
            }
            setTalkRecyclerView()
            setOnClickIbBottom()
        }.start()
    }

    private fun setTalkRecyclerView(){
        runOnUiThread{
            adapter = TalkRecyclerViewAdapter(talkItemList, screenWidth)
            binding.rvTalkList.adapter = adapter
            layoutManager.stackFromEnd = true
            binding.rvTalkList.layoutManager = layoutManager

            //새톡 위치로 이동
            var newPosition = talkItemList.size-1 -newCnt
            if(newPosition<0) newPosition = 0
            //Log.d("카카오 새톡", newPosition.toString())
            layoutManager?.let {
                it.scrollToPositionWithOffset(newPosition,0)
            }



            //스크롤 이벤트 설정
            binding.rvTalkList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                    val itemCount = adapter.itemCount
                    if(lastVisibleItemPosition == itemCount-1){
                        binding.tvNew.visibility = View.INVISIBLE
                    }
                }
            })
        }
    }

}