package com.kun.zflipcoverkakopreview.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kun.zflipcoverkakopreview.R
import com.kun.zflipcoverkakopreview.databinding.ItemRoomBinding
import com.kun.zflipcoverkakopreview.db.entity.Room
import java.time.LocalDateTime

class RoomRecyclerViewAdapter(private val roomList :ArrayList<Room>, private val roomClickListener: OnRoomClickListener) : RecyclerView.Adapter<RoomRecyclerViewAdapter.MyViewHolder>() {

    inner class MyViewHolder(binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root){
        val tv_name = binding.tvName
        val tv_talk = binding.tvTalk
        val tv_recent_dt = binding.tvRecentDt
        val tv_is_read = binding.tvIsRead
        val iv_profile = binding.ivProfile
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding : ItemRoomBinding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val room = roomList[position]

        holder.tv_name.text = room.roomName
        holder.tv_talk.text = room.recentChat
        holder.tv_recent_dt.text = room.recentDt?.let { getTimeText(it) }
        //Log.d("시간", room.recentDt.toString()) //2023-07-03T17:00:36.241061
        if(room.roomImg==null){
            holder.iv_profile.setImageResource(R.drawable.profile)
        }else{
            holder.iv_profile.setImageBitmap(room.roomImg)
        }

        if(room.newCnt>0){
            holder.tv_is_read.visibility = View.VISIBLE
            if(room.newCnt>300)
                holder.tv_is_read.text="300+"
            else
                holder.tv_is_read.text = room.newCnt.toString()
        }else{
            holder.tv_is_read.visibility = View.INVISIBLE
        }

        holder.root.setOnClickListener{
            val room = roomList[position]
            val roomId = room.id
            val newCnt = room.newCnt
            roomClickListener.onClick(roomId, newCnt)
        }

        holder.root.setOnLongClickListener {
            val roomId = roomList[position].id
            roomClickListener.onLongClick(roomId)
            true
        }
    }

    private fun getTimeText(time : LocalDateTime) : String{
        val now = LocalDateTime.now()
        if(now.minusDays(1).dayOfMonth == time.dayOfMonth) return "어제"
        val day = time.dayOfMonth
        val month = time.month.value

        if(now.minusDays(1).dayOfMonth > time.dayOfMonth) return "${month}월${day}일"
        val hour = time.hour
        val min = time.minute
        val minStr = if(min>9) "${min}" else "0${min}"
        if(hour>12) return "오후 ${hour-12}:${minStr}"
        else return "오전 ${hour}:${minStr}"

    }
}