package com.example.musicandmoney.activities

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class Application : Application() {

    companion object{
        const val CHANNEL_ID = "Channel1"
        const val NEXT = "Next"
        const val PLAY = "Play"
        const val PAUSE= "Pause"
        const val PREVIOUS = "Previous"
        const val EXIT = "Exit"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, "Music Store", importance)
        channel.description = "Now Playing"
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}