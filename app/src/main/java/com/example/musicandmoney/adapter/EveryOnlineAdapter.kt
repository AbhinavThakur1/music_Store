package com.example.musicandmoney.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicandmoney.R
import com.example.musicandmoney.model.MusicData

class EveryOnlineAdapter(private val context: Context,private val list: ArrayList<MusicData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener : OnClickListener ?= null
    private var onLongClickListener : OnLongClickListener ?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.music_background_layout,
                parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder) {
            holder.itemView.findViewById<TextView>(R.id.music_name).text = model.title
            holder.itemView.findViewById<TextView>(R.id.artist_name).text = model.artistName
            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onclick(position,model)
                }
            }
            holder.itemView.setOnLongClickListener() {
                if(onLongClickListener!=null){
                    onLongClickListener!!.onLongClick(position, model)
                }
                true
            }
        }
    }

    fun setOnclickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onclick(position: Int, model: MusicData)
    }
    fun setOnLongClickListener(onLongClickListener: OnLongClickListener){
        this.onLongClickListener = onLongClickListener
    }
    interface OnLongClickListener {
        fun onLongClick(position: Int, model: MusicData)
    }
    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view : View):RecyclerView.ViewHolder(view)

}