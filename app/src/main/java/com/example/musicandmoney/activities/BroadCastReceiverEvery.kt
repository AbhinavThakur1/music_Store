package com.example.musicandmoney.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import com.example.musicandmoney.R
import com.example.musicandmoney.model.MusicData

class BroadCastReceiverEvery : BroadcastReceiver(), MediaPlayer.OnCompletionListener{
    var conText : Context?=null

    override fun onReceive(context: Context, intent: Intent?) {
        conText = context
        when (intent!!.action) {
            Application.PREVIOUS -> {
                EveryMusicActivity.song!!.stop()
                if (EveryMusicActivity.positionMusic > 0) {
                    EveryMusicActivity.positionMusic -= 1
                    previous(
                        EveryMusicActivity.musicEveryList[EveryMusicActivity.positionMusic],
                        context
                    )
                    NotificationChannelEvery(context).showNotification(
                        EveryMusicActivity.musicEveryList[EveryMusicActivity.positionMusic],
                        "Pause"
                    )
                }
                else if(EveryMusicActivity.positionMusic == 0){
                    EveryMusicActivity.positionMusic = EveryMusicActivity.musicEveryList.size - 1
                    previous(
                        EveryMusicActivity.musicEveryList[EveryMusicActivity.positionMusic],
                        context
                    )
                    NotificationChannelEvery(context).showNotification(
                        EveryMusicActivity.musicEveryList[EveryMusicActivity.positionMusic],
                        "Pause"
                    )
                }
            }
            Application.PLAY -> {
                play()
                NotificationChannelEvery(context).showNotification(EveryMusicActivity.musicEveryList[EveryMusicActivity.positionMusic],"Pause")
            }
            Application.PAUSE -> {
                pause()
                NotificationChannelEvery(context).showNotification(EveryMusicActivity.musicEveryList[EveryMusicActivity.positionMusic],"Play")
            }
            Application.NEXT -> {
                EveryMusicActivity.song!!.stop()
                if ( EveryMusicActivity.positionMusic < EveryMusicActivity.musicEveryList.size-1){
                    EveryMusicActivity.positionMusic += 1
                    next(EveryMusicActivity.musicEveryList[EveryMusicActivity.positionMusic],context)
                    NotificationChannelEvery(context).showNotification(EveryMusicActivity.musicEveryList[EveryMusicActivity.positionMusic],"Pause")
                }

                else if(EveryMusicActivity.positionMusic ==EveryMusicActivity.musicEveryList.size -1){
                    EveryMusicActivity.positionMusic = 0
                    next(EveryMusicActivity.musicEveryList[EveryMusicActivity.positionMusic],context)
                    NotificationChannelEvery(context).showNotification(EveryMusicActivity.musicEveryList[EveryMusicActivity.positionMusic],"Pause")
                }

            }
        }
    }

    private fun play(){
        EveryMusicActivity.song!!.start()
        EveryMusicActivity.binding!!.PLayingEvery.setImageResource(R.drawable.pause)
    }

    private fun pause(){
        EveryMusicActivity.song!!.pause()
        EveryMusicActivity.binding!!.PLayingEvery.setImageResource(R.drawable.play)
    }

    private fun next(next : MusicData, context: Context){
        EveryMusicActivity.binding!!.MusicSeekBarEvery.progress = 0
        EveryMusicActivity.binding!!.EveryMusicPlayName.text = next.title
        EveryMusicActivity.binding!!.MusicSeekBarEvery.max = next.duration.toInt()
        Handler().postDelayed(object: Runnable{
            override fun run() {
                try {
                    EveryMusicActivity.binding!!.MusicSeekBarEvery.progress =
                        EveryMusicActivity.song!!.currentPosition
                    Handler().postDelayed(this,1000)
                }catch (e:Exception){
                    EveryMusicActivity.binding!!.MusicSeekBarEvery.progress = 0
                }
            }
        },0)
        EveryMusicActivity.binding!!.MusicEndTimeEvery.text = String.format("%.2f",(next.duration.toInt() / 1000).toDouble()/60)
        EveryMusicActivity.song = MediaPlayer.create(context, Uri.parse(next.path))
        EveryMusicActivity.song!!.start()
        EveryMusicActivity.song!!.isLooping = false
        EveryMusicActivity.song!!.setOnCompletionListener(this)
    }

    private fun previous(last : MusicData, context: Context){
        EveryMusicActivity.binding!!.MusicSeekBarEvery.progress = 0
        EveryMusicActivity.binding!!.EveryMusicPlayName.text = last.title
        EveryMusicActivity.binding!!.MusicSeekBarEvery.max = last.duration.toInt()
        Handler().postDelayed(object: Runnable{
            override fun run() {
                try {
                    EveryMusicActivity.binding!!.MusicSeekBarEvery.progress = LocalMusicActivity.song!!.currentPosition
                    Handler().postDelayed(this,1000)
                }catch (e:Exception){
                    EveryMusicActivity.binding!!.MusicSeekBarEvery.progress = 0
                }
            }
        },0)
        EveryMusicActivity.binding!!.MusicEndTimeEvery.text = String.format("%.2f",(last.duration.toInt() / 1000).toDouble()/60)
        EveryMusicActivity.song = MediaPlayer.create(context, Uri.parse(last.path))
        EveryMusicActivity.song!!.start()
        EveryMusicActivity.song!!.isLooping = false
        EveryMusicActivity.song!!.setOnCompletionListener(this)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        var next : MusicData?=null
        val list = EveryMusicActivity.musicEveryList
        if(list.size -1   > EveryMusicActivity.positionMusic) {
            EveryMusicActivity.positionMusic +=1
            next = list[EveryMusicActivity.positionMusic]
            next(next,conText!!)
            NotificationChannelEvery(conText!!).showNotification(next!!,"Pause")
        }else if(list.size -1 == EveryMusicActivity.positionMusic) {
            EveryMusicActivity.positionMusic = 0
            next = list[EveryMusicActivity.positionMusic]
            NotificationChannelEvery(conText!!).showNotification(next!!,"Pause")
            next(next!!,conText!!)
        }
        if (EveryMusicActivity.song!!.isPlaying){
            EveryMusicActivity.binding!!.PLayingEvery.setImageResource(R.drawable.pause)
            NotificationChannelEvery(conText!!).showNotification(next!!,"Pause")
        }
        else{
            NotificationChannelEvery(conText!!).showNotification(next!!,"Play")
        }
    }
}