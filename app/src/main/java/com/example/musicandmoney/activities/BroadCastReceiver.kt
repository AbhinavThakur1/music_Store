package com.example.musicandmoney.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import com.example.musicandmoney.R
import com.example.musicandmoney.model.MusicData


class BroadCastReceiver : BroadcastReceiver(),MediaPlayer.OnCompletionListener{
    var conText : Context ?=null

    override fun onReceive(context: Context, intent: Intent?) {
        conText = context
        when (intent!!.action) {
            Application.PREVIOUS -> {
                LocalMusicActivity.song!!.stop()
                if (LocalMusicActivity.positionMusic > 0) {
                    LocalMusicActivity.positionMusic -= 1
                    previous(
                        LocalMusicActivity.musicLocalList[LocalMusicActivity.positionMusic],
                        context
                    )
                    NotificationChannel(context).showNotification(
                        LocalMusicActivity.musicLocalList[LocalMusicActivity.positionMusic],
                        "Pause"
                    )
                }
                else if(LocalMusicActivity.positionMusic == 0){
                    LocalMusicActivity.positionMusic = LocalMusicActivity.musicLocalList.size - 1
                    previous(
                        LocalMusicActivity.musicLocalList[LocalMusicActivity.positionMusic],
                        context
                    )
                    NotificationChannel(context).showNotification(
                        LocalMusicActivity.musicLocalList[LocalMusicActivity.positionMusic],
                        "Pause"
                    )
                }
            }
            Application.PLAY -> {
                play()
                NotificationChannel(context).showNotification(LocalMusicActivity.musicLocalList[LocalMusicActivity.positionMusic],"Pause")
            }
            Application.PAUSE -> {
                pause()
                NotificationChannel(context).showNotification(LocalMusicActivity.musicLocalList[LocalMusicActivity.positionMusic],"Play")
            }
            Application.NEXT -> {
                LocalMusicActivity.song!!.stop()
                if ( LocalMusicActivity.positionMusic < LocalMusicActivity.musicLocalList.size-1){
                    LocalMusicActivity.positionMusic += 1
                    next(LocalMusicActivity.musicLocalList[LocalMusicActivity.positionMusic],context)
                    NotificationChannel(context).showNotification(LocalMusicActivity.musicLocalList[LocalMusicActivity.positionMusic],"Pause")
                }

                else if(LocalMusicActivity.positionMusic == LocalMusicActivity.musicLocalList.size -1){
                    LocalMusicActivity.positionMusic = 0
                    next(LocalMusicActivity.musicLocalList[LocalMusicActivity.positionMusic],context)
                    NotificationChannel(context).showNotification(LocalMusicActivity.musicLocalList[LocalMusicActivity.positionMusic],"Pause")
                }

            }
        }
    }

    private fun play(){
        LocalMusicActivity.song!!.start()
        LocalMusicActivity.binding!!.PLayingLocal.setImageResource(R.drawable.pause)
    }

    private fun pause(){
        LocalMusicActivity.song!!.pause()
        LocalMusicActivity.binding!!.PLayingLocal.setImageResource(R.drawable.play)
    }

    private fun next(next : MusicData,context: Context){
        LocalMusicActivity.binding!!.MusicSeekBarLocal.progress = 0
        LocalMusicActivity.binding!!.LocalMusicPlayName.text = next.title
        LocalMusicActivity.binding!!.MusicSeekBarLocal.max = next.duration.toInt()
        Handler().postDelayed(object: Runnable{
            override fun run() {
                try {
                    LocalMusicActivity.binding!!.MusicSeekBarLocal.progress =
                        LocalMusicActivity.song!!.currentPosition
                    Handler().postDelayed(this,1000)
                }catch (e:Exception){
                    LocalMusicActivity.binding!!.MusicSeekBarLocal.progress = 0
                }
            }
        },0)
        LocalMusicActivity.binding!!.MusicEndTimeLocal.text = String.format("%.2f",(next.duration.toInt() / 1000).toDouble()/60)
        LocalMusicActivity.song = MediaPlayer.create(context, Uri.parse(next.path))
        LocalMusicActivity.song!!.start()
        LocalMusicActivity.song!!.isLooping = false
        LocalMusicActivity.song!!.setOnCompletionListener(this)
    }

    private fun previous(last : MusicData,context: Context){
        LocalMusicActivity.binding!!.MusicSeekBarLocal.progress = 0
        LocalMusicActivity.binding!!.LocalMusicPlayName.text = last.title
        LocalMusicActivity.binding!!.MusicSeekBarLocal.max = last.duration.toInt()
        Handler().postDelayed(object: Runnable{
            override fun run() {
                try {
                    LocalMusicActivity.binding!!.MusicSeekBarLocal.progress = LocalMusicActivity.song!!.currentPosition
                    Handler().postDelayed(this,1000)
                }catch (e:Exception){
                    LocalMusicActivity.binding!!.MusicSeekBarLocal.progress = 0
                }
            }
        },0)
        LocalMusicActivity.binding!!.MusicEndTimeLocal.text = String.format("%.2f",(last.duration.toInt() / 1000).toDouble()/60)
        LocalMusicActivity.song = MediaPlayer.create(context, Uri.parse(last.path))
        LocalMusicActivity.song!!.start()
        LocalMusicActivity.song!!.isLooping = false
        LocalMusicActivity.song!!.setOnCompletionListener(this)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        var next : MusicData ?=null
        val list = LocalMusicActivity.musicLocalList
        if(list.size -1   > LocalMusicActivity.positionMusic) {
            LocalMusicActivity.positionMusic +=1
            next = list[LocalMusicActivity.positionMusic]
            next(next,conText!!)
            NotificationChannel(conText!!).showNotification(next!!,"Pause")
        }else if(list.size -1 == LocalMusicActivity.positionMusic) {
            LocalMusicActivity.positionMusic = 0
            next = list[LocalMusicActivity.positionMusic]
            NotificationChannel(conText!!).showNotification(next!!,"Pause")
            next(next!!,conText!!)
        }
        if (LocalMusicActivity.song!!.isPlaying){
            LocalMusicActivity.binding!!.PLayingLocal.setImageResource(R.drawable.pause)
            NotificationChannel(conText!!).showNotification(next!!,"Pause")
        }
        else{
            NotificationChannel(conText!!).showNotification(next!!,"Play")
        }
    }
}