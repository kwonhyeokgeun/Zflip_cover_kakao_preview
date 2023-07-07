package com.example.zflipcoverkakopreview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zflipcoverkakopreview.databinding.ItemTalkBinding
import com.example.zflipcoverkakopreview.db.entity.Talk
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TalkRecyclerViewAdapter (private val talkList : List<Talk>, private val screenWidth : Int) : RecyclerView.Adapter<TalkRecyclerViewAdapter.MyViewHolder>(){
    inner class MyViewHolder(binding : ItemTalkBinding) : RecyclerView.ViewHolder(binding.root){
        val tv_name = binding.tvName
        val tv_chat = binding.tvChat
        val tv_time = binding.tvTime
        val iv_profile = binding.ivProfile
        val cv_profile = binding.cvProfile
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding : ItemTalkBinding = ItemTalkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return talkList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val talk = talkList[position]

        holder.tv_chat.maxWidth = (screenWidth*0.6).toInt()

        //위랑 비교, 동일인물
        if( position>0 && talkList[position-1].name == talk.name){
            val prevTime = talkList[position-1].regDt
            val time = talk.regDt
            //2분이상 차이
            if(prevTime.isBefore(time.minus(1, ChronoUnit.MINUTES))){
                holder.cv_profile.layoutParams.height =holder.cv_profile.layoutParams.width
                holder.cv_profile.visibility = View.VISIBLE
                holder.tv_name.visibility = View.VISIBLE
                holder.tv_name.text = talk.name
            }else{
                holder.cv_profile.layoutParams.height =holder.cv_profile.layoutParams.width/2
                holder.cv_profile.visibility = View.INVISIBLE
                holder.tv_name.visibility = View.GONE
            }
        }
        else{ //다른사람
            holder.tv_name.visibility = View.VISIBLE
            holder.cv_profile.layoutParams.height =holder.cv_profile.layoutParams.width
            holder.cv_profile.visibility = View.VISIBLE

            holder.tv_name.text = talk.name
        }
        holder.tv_chat.text = talk.chat

        //아래랑 비교
        if(position!=talkList.size-1 && talkList[position+1].name == talk.name){
            val nextTime = talkList[position+1].regDt
            val time = talk.regDt
            //동일시간
            if(nextTime.minute==time.minute && nextTime.hour == time.hour && nextTime.dayOfMonth == time.dayOfMonth) {
                holder.tv_time.visibility = View.GONE
            }else{
                holder.tv_time.visibility = View.VISIBLE
                holder.tv_time.text = getTimeText(talk.regDt)
            }
        }else{
            holder.tv_time.visibility = View.VISIBLE
            holder.tv_time.text = getTimeText(talk.regDt)
        }

    }

    private fun getTimeText(regDt : LocalDateTime) : String{
        val hour = regDt.hour
        val min = regDt.minute
        var hourStr : String
        var minStr : String
        var ampm : String
        if(hour>12) {
            hourStr="${hour-12}"
            ampm="오후"
        }else{
            hourStr="${hour}"
            ampm="오전"
        }
        if(min<10) minStr="0${min}" else minStr="${min}"
        return "${ampm} ${hourStr}:${minStr}"
    }
}