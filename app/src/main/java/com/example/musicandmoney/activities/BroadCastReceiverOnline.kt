package com.example.musicandmoney.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import com.example.musicandmoney.R
import com.example.musicandmoney.model.MusicData

class BroadCastReceiverOnline: BroadcastReceiver(), MediaPlayer.OnCompletionListener{
    var conText : Context?=null

    override fun onReceive(context: Context, intent: Intent?) {
        conText = context
        when (intent!!.action) {
            Application.PREVIOUS -> {
                OnlineMusicActivity.song!!.stop()
                if (OnlineMusicActivity.positionMusic > 0) {
                    OnlineMusicActivity.positionMusic -= 1
                    previous(
                        OnlineMusicActivity.musicOnlineList[OnlineMusicActivity.positionMusic],
                        context
                    )
                    NotificationChannelOnline(context).showNotification(
                        OnlineMusicActivity.musicOnlineList[OnlineMusicActivity.positionMusic],
                        "Pause"
                    )
                }
                else if(OnlineMusicActivity.positionMusic == 0){
                    OnlineMusicActivity.positionMusic = OnlineMusicActivity.musicOnlineList.size - 1
                    previous(
                        OnlineMusicActivity.musicOnlineList[OnlineMusicActivity.positionMusic],
                        context
                    )
                    NotificationChannelOnline(context).showNotification(
                        OnlineMusicActivity.musicOnlineList[OnlineMusicActivity.positionMusic],
                        "Pause"
                    )
                }
            }
            Application.PLAY -> {
                play()
                NotificationChannelOnline(context).showNotification(OnlineMusicActivity.musicOnlineList[OnlineMusicActivity.positionMusic],"Pause")
            }
            Application.PAUSE -> {
                pause()
                NotificationChannelOnline(context).showNotification(OnlineMusicActivity.musicOnlineList[OnlineMusicActivity.positionMusic],"Play")
            }
            Application.NEXT -> {
                OnlineMusicActivity.song!!.stop()
                if ( OnlineMusicActivity.positionMusic < OnlineMusicActivity.musicOnlineList.size-1){
                    OnlineMusicActivity.positionMusic += 1
                    next(OnlineMusicActivity.musicOnlineList[OnlineMusicActivity.positionMusic],context)
                    NotificationChannelOnline(context).showNotification(OnlineMusicActivity.musicOnlineList[OnlineMusicActivity.positionMusic],"Pause")
                }

                else if(OnlineMusicActivity.positionMusic == OnlineMusicActivity.musicOnlineList.size -1){
                    OnlineMusicActivity.positionMusic = 0
                    next(OnlineMusicActivity.musicOnlineList[OnlineMusicActivity.positionMusic],context)
                    NotificationChannelOnline(context).showNotification(OnlineMusicActivity.musicOnlineList[OnlineMusicActivity.positionMusic],"Pause")
                }

            }
        }
    }

    private fun play(){
        OnlineMusicActivity.song!!.start()
        OnlineMusicActivity.binding!!.PLayingOnline.setImageResource(R.drawable.pause)
    }

    private fun pause(){
        OnlineMusicActivity.song!!.pause()
        OnlineMusicActivity.binding!!.PLayingOnline.setImageResource(R.drawable.play)
    }

    private fun next(next : MusicData, context: Context){
        OnlineMusicActivity.binding!!.MusicSeekBarOnline.progress = 0
        OnlineMusicActivity.binding!!.OnlineMusicPlayName.text = next.title
        OnlineMusicActivity.binding!!.MusicSeekBarOnline.max = next.duration.toInt()
        Handler().postDelayed(object: Runnable{
            override fun run() {
                try {
                    OnlineMusicActivity.binding!!.MusicSeekBarOnline.progress =
                        OnlineMusicActivity.song!!.currentPosition
                    Handler().postDelayed(this,1000)
                }catch (e:Exception){
                    OnlineMusicActivity.binding!!.MusicSeekBarOnline.progress = 0
                }
            }
        },0)
        OnlineMusicActivity.binding!!.MusicEndTimeOnline.text = String.format("%.2f",(next.duration.toInt() / 1000).toDouble()/60)
        OnlineMusicActivity.song = MediaPlayer.create(context, Uri.parse(next.path))
        OnlineMusicActivity.song!!.start()
        OnlineMusicActivity.song!!.isLooping = false
        OnlineMusicActivity.song!!.setOnCompletionListener(this)
    }

    private fun previous(last : MusicData, context: Context){
        OnlineMusicActivity.binding!!.MusicSeekBarOnline.progress = 0
        OnlineMusicActivity.binding!!.OnlineMusicPlayName.text = last.title
        OnlineMusicActivity.binding!!.MusicSeekBarOnline.max = last.duration.toInt()
        Handler().postDelayed(object: Runnable{
            override fun run() {
                try {
                    OnlineMusicActivity.binding!!.MusicSeekBarOnline.progress = OnlineMusicActivity.song!!.currentPosition
                    Handler().postDelayed(this,1000)
                }catch (e:Exception){
                    OnlineMusicActivity.binding!!.MusicSeekBarOnline.progress = 0
                }
            }
        },0)
        OnlineMusicActivity.binding!!.MusicEndTimeOnline.text = String.format("%.2f",(last.duration.toInt() / 1000).toDouble()/60)
        OnlineMusicActivity.song = MediaPlayer.create(context, Uri.parse(last.path))
        OnlineMusicActivity.song!!.start()
        OnlineMusicActivity.song!!.isLooping = false
        OnlineMusicActivity.song!!.setOnCompletionListener(this)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        var next : MusicData?=null
        val list = OnlineMusicActivity.musicOnlineList
        if(list.size -1   > OnlineMusicActivity.positionMusic) {
            OnlineMusicActivity.positionMusic +=1
            next = list[OnlineMusicActivity.positionMusic]
            next(next,conText!!)
            NotificationChannelOnline(conText!!).showNotification(next!!,"Pause")
        }else if(list.size -1 == OnlineMusicActivity.positionMusic) {
            OnlineMusicActivity.positionMusic = 0
            next = list[OnlineMusicActivity.positionMusic]
            NotificationChannelOnline(conText!!).showNotification(next!!,"Pause")
            next(next!!,conText!!)
        }
        if (OnlineMusicActivity.song!!.isPlaying){
            OnlineMusicActivity.binding!!.PLayingOnline.setImageResource(R.drawable.pause)
            NotificationChannelOnline(conText!!).showNotification(next!!,"Pause")
        }
        else{
            NotificationChannelOnline(conText!!).showNotification(next!!,"Play")
        }
    }
}