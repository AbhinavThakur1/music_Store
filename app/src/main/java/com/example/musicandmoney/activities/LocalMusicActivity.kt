package com.example.musicandmoney.activities

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicandmoney.R
import com.example.musicandmoney.adapter.MainActivityRecycleViewAdapter
import com.example.musicandmoney.databinding.ActivityLocalMusicBinding
import com.example.musicandmoney.model.MusicData
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class LocalMusicActivity : AppCompatActivity() , MediaPlayer.OnCompletionListener{
    companion object{
        var song : MediaPlayer? = null
        var musicLocalList = ArrayList<MusicData>()
        var positionMusic: Int = 0
        @SuppressLint("StaticFieldLeak")
        var binding : ActivityLocalMusicBinding ?= null
    }
    var context = this@LocalMusicActivity
    private var next: MusicData? = null
    private var last: MusicData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocalMusicBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setUpActionBar()
        localMusic()
        checkIfUserIsLoggedIn()
        binding!!.toolbarLocalMusic.setNavigationOnClickListener {
            onBackPressed()
            song = null
        }
        binding!!.MusicSeekBarLocal.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)song?.seekTo(progress)
                binding!!.MusicTimeCounterLocal.text = String.format("%.2f",(seekBar!!.progress / 1000).toDouble()/60)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })


    }

    private fun setUpActionBar(){
        setSupportActionBar(binding!!.toolbarLocalMusic)
        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.back)
    }

    private fun checkIfUserIsLoggedIn() :  String{
        val currentUser = FirebaseAuth.getInstance().currentUser
        var isLoggedIn = ""
        if(currentUser != null){
            isLoggedIn = currentUser.uid
        }
        return isLoggedIn
    }

    private fun localMusic() {
        val fireStore = FirebaseStorage.getInstance()
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE
        )
        val cursor: Cursor =
            contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null)!!
        while (cursor.moveToNext()) {
            if (cursor.getString(1).contains(".mp3") && cursor.getString(2) != null) {
                val song = MusicData(
                    cursor.getString(0),
                    cursor.getString(4),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(1)
                )
                val musicName = cursor.getString(1)
                val file = Uri.fromFile(File(cursor.getString(0)))
                if (checkIfUserIsLoggedIn().isNotEmpty()) {
                    fireStore.reference.storage.reference.child(checkIfUserIsLoggedIn() + "/$musicName")
                        .putFile(file)
                }
                fireStore.reference.child("EveryMusic/$musicName").putFile(file)

                musicLocalList.add(song)
            }
            setupLocalRecycleView(musicLocalList)

            if (musicLocalList.isNotEmpty()){
                listChecker(musicLocalList)
                binding!!.LocalMusicHandler.visibility = View.VISIBLE
                binding!!.NoData.visibility = View.GONE
                listChecker(musicLocalList)
            }else{
                binding!!.LocalMusicHandler.visibility = View.GONE
                binding!!.NoData.visibility = View.VISIBLE
            }
        }
        cursor.close()
        for (music in musicLocalList) {
            fireStore.reference.storage.reference.child("${checkIfUserIsLoggedIn()}/${music.fileName}").downloadUrl.addOnSuccessListener(
                this,
                OnSuccessListener<Uri>() { uri ->
                    val musicOnline=MusicData(
                        uri.toString(),
                        music.title,
                        music.duration,
                        music.artistName,
                        music.fileName
                    )
                    FirebaseFirestore.getInstance()
                        .collection(checkIfUserIsLoggedIn())
                        .document(music.fileName).set(musicOnline)
                })
            FirebaseStorage.getInstance().reference.child("EveryMusic/${music.fileName}").downloadUrl.addOnSuccessListener(
                this,
                OnSuccessListener<Uri>() { uri ->
                    val everyMusicOnline=MusicData(
                        uri.toString(),
                        music.title,
                        music.duration,
                        music.artistName,
                        music.fileName
                    )
                    FirebaseFirestore.getInstance().collection("Every Music")
                        .document(music.fileName)
                        .set(everyMusicOnline)
                })
        }
    }

    private fun setupLocalRecycleView(list : ArrayList<MusicData>){
        val recyclerView = binding!!.LocalMusicHandler
        val adapter =  MainActivityRecycleViewAdapter(this,list)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter.setOnclickListener(object : MainActivityRecycleViewAdapter.OnClickListener{
            override fun onclick(position: Int, model: MusicData) {
                if(song!= null){
                    song!!.reset()
                }
                NotificationChannel(applicationContext).showNotification(model,"Pause")
                binding!!.MusicSeekBarLocal.progress = 0
                song = MediaPlayer.create(applicationContext, Uri.parse(model.path))
                song!!.start()
                song!!.isLooping = false
                song!!.setOnCompletionListener(this@LocalMusicActivity)
                positionMusic = position
                binding!!.PLayingLocal.setImageResource(R.drawable.pause)
                binding!!.LocalMusicPlayName.text = model.title
                binding!!.MusicSeekBarLocal.max = model.duration.toInt()
                Handler().postDelayed(object: Runnable{
                    override fun run() {
                        try {
                            binding!!.MusicSeekBarLocal.progress = song!!.currentPosition
                            Handler().postDelayed(this,1000)
                        }catch (e:Exception){
                            binding!!.MusicSeekBarLocal.progress = 0
                        }
                    }
                },0)
                binding!!.MusicEndTimeLocal.text = String.format("%.2f",(model.duration.toInt() / 1000).toDouble()/60)
                binding!!.CardViewLocalMusic.visibility = View.VISIBLE
            }
        })
    }

    private fun setResourceForNextSong(next :MusicData){
        binding!!.MusicSeekBarLocal.progress = 0
        binding!!.LocalMusicPlayName.text = next.title
        binding!!.MusicSeekBarLocal.max = next.duration.toInt()
        Handler().postDelayed(object: Runnable{
            override fun run() {
                try {
                    binding!!.MusicSeekBarLocal.progress =song!!.currentPosition
                    Handler().postDelayed(this,1000)
                }catch (e:Exception){
                    binding!!.MusicSeekBarLocal.progress = 0
                }
            }
        },0)
        binding!!.MusicEndTimeLocal.text = String.format("%.2f",(next.duration.toInt() / 1000).toDouble()/60)
        song = MediaPlayer.create(applicationContext, Uri.parse(next.path))
        song!!.start()
        song!!.isLooping = false
        song!!.setOnCompletionListener(this)

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        musicLocalList = ArrayList<MusicData>()
        if (song!=null) {
            song!!.stop()
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }
    }

    private fun setResourceLastMusic(last : MusicData){
        binding!!.MusicSeekBarLocal.progress = 0
        binding!!.LocalMusicPlayName.text = last.title
        binding!!.MusicSeekBarLocal.max = last.duration.toInt()
        Handler().postDelayed(object: Runnable{
            override fun run() {
                try {
                    binding!!.MusicSeekBarLocal.progress = song!!.currentPosition
                    Handler().postDelayed(this,1000)
                }catch (e:Exception){
                    binding!!.MusicSeekBarLocal.progress = 0
                }
            }
        },0)
        binding!!.MusicEndTimeLocal.text = String.format("%.2f",(last.duration.toInt() / 1000).toDouble()/60)
        song = MediaPlayer.create(applicationContext, Uri.parse(last.path))
        song!!.start()
        song!!.isLooping = false
        song!!.setOnCompletionListener(this)
    }

    private fun listChecker(list:ArrayList<MusicData>){

        binding!!.PLayingLocal.setOnClickListener {
            if (song!!.isPlaying)
            { song!!.pause()
                val now =musicLocalList[positionMusic]
                NotificationChannel(applicationContext).showNotification(now,"Play")
                binding!!.PLayingLocal.setImageResource(R.drawable.play)
            }
            else{
                song!!.start()
                val now =musicLocalList[positionMusic]
                binding!!.PLayingLocal.setImageResource(R.drawable.pause)
                NotificationChannel(applicationContext).showNotification(now,"Pause")
            }
        }

        binding!!.NextSongLocal.setOnClickListener {
            song!!.reset()
            if(list.size -1  > positionMusic) {
                positionMusic+=1
                next = list[positionMusic]
                NotificationChannel(applicationContext).showNotification(next!!,"Pause")
                setResourceForNextSong(next!!)
            }else if(list.size -1 == positionMusic) {
                positionMusic = 0
                next = list[positionMusic]
                NotificationChannel(applicationContext).showNotification(next!!,"Pause")
                setResourceForNextSong(next!!)
            }
            if (song!!.isPlaying){
                binding!!.PLayingLocal.setImageResource(R.drawable.pause)
                NotificationChannel(applicationContext).showNotification(next!!,"Pause")
            }
            else{
                binding!!.PLayingLocal.setImageResource(R.drawable.play)
                NotificationChannel(applicationContext).showNotification(next!!,"Play")
            }

        }

        binding!!.PreviousSongLocal.setOnClickListener {
            song!!.reset()
            if (positionMusic  > 0) {
                positionMusic-=1
                last = list[positionMusic]
                NotificationChannel(applicationContext).showNotification(last!!,"Pause")
                setResourceLastMusic(last!!)

            }else if (positionMusic == 0){
                positionMusic = list.size -1
                last = list[positionMusic]
                NotificationChannel(applicationContext).showNotification(last!!,"Pause")
                setResourceLastMusic(last!!)
            }
            if (song!!.isPlaying){
                binding!!.PLayingLocal.setImageResource(R.drawable.pause)
                NotificationChannel(applicationContext).showNotification(last!!,"Pause")

            }
            else{
                binding!!.PLayingLocal.setImageResource(R.drawable.play)
                NotificationChannel(applicationContext).showNotification(last!!,"Play")
            }
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        val list = musicLocalList
        if(list.size -1   > positionMusic) {
            positionMusic+=1
            next = list[positionMusic]
            setResourceForNextSong(next!!)
            NotificationChannel(applicationContext).showNotification(next!!,"Pause")
        }else if(list.size -1 == positionMusic) {
            positionMusic = 0
            next = list[positionMusic]
            NotificationChannel(applicationContext).showNotification(next!!,"Pause")
            setResourceForNextSong(next!!)
        }
        if (song!!.isPlaying){
            binding!!.PLayingLocal.setImageResource(R.drawable.pause)
            NotificationChannel(applicationContext).showNotification(next!!,"Pause")
        }
        else{
            NotificationChannel(applicationContext).showNotification(next!!,"Play")
        }
    }
}