package com.example.musicandmoney.activities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.musicandmoney.R
import com.example.musicandmoney.model.MusicData

class NotificationChannelEvery (private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    companion object{
        const val CHANNEL_ID ="Channel1"
        const val COUNTER_CHANNEL = "Counter_channel"
    }

    fun showNotification(model : MusicData, playPause : String){

        val notification = NotificationCompat.Builder(context , COUNTER_CHANNEL)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setContentTitle(model.title)
            .setContentText(model.artistName)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setChannelId(CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                R.drawable.last_song_nf,"Previous",
                PendingIntent.getBroadcast(context,1,
                    Intent(context,BroadCastReceiverEvery::class.java).setAction(Application.PREVIOUS),
                    PendingIntent.FLAG_IMMUTABLE))
            .addAction(
                R.drawable.play_nf,playPause,
                PendingIntent.getBroadcast(context,1,
                    Intent(context,BroadCastReceiverEvery::class.java).setAction(playPause),
                    PendingIntent.FLAG_IMMUTABLE))
            .addAction(
                R.drawable.next_nf,"Next",
                PendingIntent.getBroadcast(context,1,
                    Intent(context,BroadCastReceiverEvery::class.java).setAction(Application.NEXT),
                    PendingIntent.FLAG_IMMUTABLE))
            .build()
        notificationManager.notify(1,notification)
    }
}