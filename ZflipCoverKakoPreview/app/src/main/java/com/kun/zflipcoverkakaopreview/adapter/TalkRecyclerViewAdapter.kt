package com.kun.zflipcoverkakopreview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kun.zflipcoverkakopreview.R
import com.kun.zflipcoverkakopreview.databinding.ItemTalkBinding
import com.kun.zflipcoverkakopreview.db.entity.TalkItem
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TalkRecyclerViewAdapter (private val talkItemList : List<TalkItem>, private val screenWidth : Int) : RecyclerView.Adapter<TalkRecyclerViewAdapter.MyViewHolder>(){
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
        return talkItemList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val talkItem = talkItemList[position]

        holder.tv_chat.maxWidth = (screenWidth*0.6).toInt()

        //위랑 비교, 동일인물
        if( position>0 && talkItemList[position-1].name == talkItem.name){
            val prevTime = talkItemList[position-1].regDt
            val time = talkItem.regDt
            //2분이상 차이
            if(prevTime.isBefore(time.minus(1, ChronoUnit.MINUTES))){
                holder.cv_profile.layoutParams.height =holder.cv_profile.layoutParams.width
                holder.cv_profile.visibility = View.VISIBLE
                holder.tv_name.visibility = View.VISIBLE
                holder.tv_name.text = talkItem.name
                if(talkItem.profileImg==null) holder.iv_profile.setImageResource(R.drawable.profile)
                else holder.iv_profile.setImageBitmap(talkItem.profileImg)
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
            if(talkItem.profileImg==null) holder.iv_profile.setImageResource(R.drawable.profile)
            else holder.iv_profile.setImageBitmap(talkItem.profileImg)

            holder.tv_name.text = talkItem.name
        }
        holder.tv_chat.text = talkItem.chat

        //아래랑 비교
        if(position!=talkItemList.size-1 && talkItemList[position+1].name == talkItem.name){
            val nextTime = talkItemList[position+1].regDt
            val time = talkItem.regDt
            //동일시간
            if(nextTime.minute==time.minute && nextTime.hour == time.hour && nextTime.dayOfMonth == time.dayOfMonth) {
                holder.tv_time.visibility = View.GONE
            }else{
                holder.tv_time.visibility = View.VISIBLE
                holder.tv_time.text = getTimeText(talkItem.regDt)
            }
        }else{
            holder.tv_time.visibility = View.VISIBLE
            holder.tv_time.text = getTimeText(talkItem.regDt)
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