package com.example.musicandmoney.activities

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicandmoney.R
import com.example.musicandmoney.adapter.EveryMusicSearchMusic
import com.example.musicandmoney.adapter.EveryOnlineAdapter
import com.example.musicandmoney.databinding.ActivityEveryMusicBinding
import com.example.musicandmoney.model.MusicData
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

class EveryMusicActivity : AppCompatActivity() , MediaPlayer.OnCompletionListener{
    companion object{
        var song : MediaPlayer?=null
        var positionMusic: Int = 0
        var musicEveryList = ArrayList<MusicData>()
        var musicEverySearchList = ArrayList<MusicData>()
        @SuppressLint("StaticFieldLeak")
        var binding : ActivityEveryMusicBinding ?=null
    }
    private var doubleBackPressed = false
    private var next: MusicData? = null
    private var last: MusicData? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEveryMusicBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setUpActionBar()
        checkIfUserIsLoggedIn()
        appCheck()
        fireStoreGetEveryData()
        binding!!.toolbarEveryMusic.setNavigationOnClickListener {
            onBackPressed()
            song = null
        }
        binding!!.MusicSeekBarEvery.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) song?.seekTo(progress)
                binding!!.MusicTimeCounterEvery.text = String.format("%.2f",(seekBar!!.progress / 1000).toDouble()/60)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding!!.Confirm.setOnClickListener {
            if (binding!!.searchText.text.toString().isNotEmpty()) {
                binding!!.everyMusicHandler.visibility = View.GONE
                musicEverySearchList = kotlin.collections.ArrayList<MusicData>()
                setUpEverySearchedRecycleView(musicEverySearchList)
                fireStoreGetSearchedEveryData()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        onDoubleBackPressed()
    }

    private fun onDoubleBackPressed(){
        if(doubleBackPressed){
            super.onBackPressed()
            musicEveryList = ArrayList<MusicData>()
            musicEverySearchList = ArrayList<MusicData>()
            if (song !=null) {
                song!!.stop()
                val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancelAll()
            }
            return
        }
        this.doubleBackPressed = true
        musicEverySearchList = ArrayList<MusicData>()
        binding!!.everyMusicSearchedHandler.visibility = View.GONE
        setUpEverySearchedRecycleView(musicEverySearchList)
        binding!!.everyMusicHandler.visibility = View.VISIBLE
        binding!!.NoData.visibility = View.GONE
        Handler().postDelayed({this.doubleBackPressed = false},3000)
    }

    private fun setUpActionBar(){
        setSupportActionBar(binding!!.toolbarEveryMusic)
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

    private fun appCheck(){
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }

    private fun fireStoreGetSearchedEveryData(){
        FirebaseFirestore.getInstance().collection("Every Music").get()
            .addOnSuccessListener{
                    it->
                val list :List<DocumentSnapshot> =it.documents
                for(doc in list) {
                    if (doc.data!!.getValue("title").toString().lowercase().contains(binding!!.searchText.text.toString().lowercase()))
                    {
                        val path = doc.data!!.getValue("path").toString()
                        val title = doc.data!!.getValue("title").toString()
                        val duration = doc.data!!.getValue("duration").toString()
                        val artistName = doc.data!!.getValue("artistName").toString()
                        val filename = doc.data!!.getValue("fileName").toString()
                        val combine = MusicData(path, title, duration, artistName, filename)
                        musicEverySearchList.add(combine)
                    }
                }
                setUpEverySearchedRecycleView(musicEverySearchList)
                if (musicEverySearchList.isNotEmpty()){
                    binding!!.everyMusicSearchedHandler.visibility = View.VISIBLE
                    binding!!.NoData.visibility = View.GONE
                    listChecker(musicEverySearchList)
                }
                else{
                    binding!!.everyMusicSearchedHandler.visibility = View.GONE
                    binding!!.NoData.visibility = View.VISIBLE
                }
            }
    }

    private fun fireStoreGetEveryData(){
        FirebaseFirestore.getInstance().collection("Every Music").get()
            .addOnSuccessListener{
                    it->
                val list :List<DocumentSnapshot> =it.documents
                for(doc in list){
                    val path = doc.data!!.getValue("path").toString()
                    val title = doc.data!!.getValue("title").toString()
                    val duration = doc.data!!.getValue("duration").toString()
                    val artistName = doc.data!!.getValue("artistName").toString()
                    val filename = doc.data!!.getValue("fileName").toString()
                    val combine = MusicData(path, title, duration, artistName, filename)
                    musicEveryList.add(combine)
                }
                setUpEveryRecycleView(musicEveryList)
                if (musicEveryList.isNotEmpty()){
                    binding!!.everyMusicHandler.visibility = View.VISIBLE
                    binding!!.NoData.visibility = View.GONE
                    listChecker(musicEveryList)
                }else{
                    binding!!.everyMusicHandler.visibility = View.GONE
                    binding!!.NoData.visibility = View.VISIBLE
                }
            }
    }

    private fun setUpEverySearchedRecycleView(list : ArrayList<MusicData>){
        val adapter =  EveryMusicSearchMusic(this,list)
        val recycleView = binding!!.everyMusicSearchedHandler
        recycleView.adapter = adapter
        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(this)
        adapter.setOnclickListener(object : EveryMusicSearchMusic.OnClickListener{
            override fun onclick(position: Int, model: MusicData) {
                if(song != null){
                    song!!.reset()
                    val notificationManager: NotificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancelAll()
                }
                NotificationChannelEverySearch(applicationContext).showNotification(model,"Pause")
                song = MediaPlayer.create(applicationContext, Uri.parse(model.path))
                song!!.start()
                song!!.isLooping = false
                song!!.setOnCompletionListener(this@EveryMusicActivity)
                positionMusic = position
                binding!!.PLayingEvery.setImageResource(R.drawable.pause)
                binding!!.EveryMusicPlayName.text = model.title
                binding!!.MusicSeekBarEvery.max = model.duration.toInt()
                Handler().postDelayed(object: Runnable{
                    override fun run() {
                        try {
                            binding!!.MusicSeekBarEvery.progress = song!!.currentPosition
                            Handler().postDelayed(this,1000)
                        }catch (e:Exception){
                            binding!!.MusicSeekBarEvery.progress = 0
                        }
                    }
                },0)
                binding!!.MusicEndTimeEvery .text = String.format("%.2f",(model.duration.toInt() / 1000).toDouble()/60)
                binding!!.CardViewEveryMusic.visibility = View.VISIBLE
            }
        })

        adapter.setOnLongClickListener(object : EveryMusicSearchMusic.OnLongClickListener{
            override fun onLongClick(position: Int, model: MusicData) {
                AlertDialog.Builder(this@EveryMusicActivity).setTitle("Want to add this to your collection")
                    .setPositiveButton("yes"){
                            dialog,_ ->
                        FirebaseFirestore.getInstance()
                            .collection(checkIfUserIsLoggedIn())
                            .document(model.fileName).set(model)
                    }.setNegativeButton("No"){
                            dialog,_ ->
                        dialog.dismiss()
                    }.show()

            }

        })
    }

    private fun setUpEveryRecycleView(list : ArrayList<MusicData>){
        val adapter =  EveryOnlineAdapter(this,list)
        val recycleView = binding!!.everyMusicHandler
        recycleView.adapter = adapter
        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(this)
        adapter.setOnclickListener(object : EveryOnlineAdapter.OnClickListener{
            override fun onclick(position: Int, model: MusicData) {
                if(song != null){
                    song!!.reset()
                }
                NotificationChannelEvery(applicationContext).showNotification(model!!,"Pause")
                song = MediaPlayer.create(applicationContext, Uri.parse(model.path))
                song!!.start()
                song!!.isLooping = false
                song!!.setOnCompletionListener(this@EveryMusicActivity)
                positionMusic = position
                binding!!.PLayingEvery.setImageResource(R.drawable.pause)
                binding!!.EveryMusicPlayName.text = model.title
                binding!!.MusicSeekBarEvery.max = model.duration.toInt()
                Handler().postDelayed(object: Runnable{
                    override fun run() {
                        try {
                            binding!!.MusicSeekBarEvery.progress = song!!.currentPosition
                            Handler().postDelayed(this,1000)
                        }catch (e:Exception){
                            binding!!.MusicSeekBarEvery.progress = 0
                        }
                    }
                },0)
                binding!!.MusicEndTimeEvery .text = String.format("%.2f",(model.duration.toInt() / 1000).toDouble()/60)
                binding!!.CardViewEveryMusic.visibility = View.VISIBLE
            }
        })
        adapter.setOnLongClickListener(object : EveryOnlineAdapter.OnLongClickListener{
            override fun onLongClick(position: Int, model: MusicData) {
                AlertDialog.Builder(this@EveryMusicActivity).setTitle("Want to add this to your collection")
                    .setPositiveButton("yes"){
                            dialog,_ ->
                        FirebaseFirestore.getInstance()
                            .collection(checkIfUserIsLoggedIn())
                            .document(model.fileName).set(model)
                    }.setNegativeButton("No"){
                            dialog,_ ->
                        dialog.dismiss()
                    }.show()

            }

        })
    }

    private fun setResourceForNextSong(next :MusicData){
        binding!!.MusicSeekBarEvery.progress = 0
        binding!!.EveryMusicPlayName.text = next.title
        binding!!.MusicSeekBarEvery.max = next.duration.toInt()
        Handler().postDelayed(object: Runnable{
            override fun run() {
                try {
                    binding!!.MusicSeekBarEvery.progress = song!!.currentPosition
                    Handler().postDelayed(this,1000)
                }catch (e:Exception){
                    binding!!.MusicSeekBarEvery.progress = 0
                }
            }
        },0)
        binding!!.MusicEndTimeEvery.text = String.format("%.2f",(next.duration.toInt() / 1000).toDouble()/60)
        song = MediaPlayer.create(applicationContext, Uri.parse(next.path))
        song!!.start()
        song!!.isLooping = false
        song!!.setOnCompletionListener(this)
    }

    private fun setResourceLastMusic(last : MusicData){
        binding!!.MusicSeekBarEvery.progress = 0
        binding!!.EveryMusicPlayName.text = last.title
        binding!!.MusicSeekBarEvery.max = last.duration.toInt()
        Handler().postDelayed(object: Runnable{
            override fun run() {
                try {
                    binding!!.MusicSeekBarEvery.progress = song!!.currentPosition
                    Handler().postDelayed(this,1000)
                }catch (e:Exception){
                    binding!!.MusicSeekBarEvery.progress = 0
                }
            }
        },0)
        binding!!.MusicEndTimeEvery.text = String.format("%.2f",(last.duration.toInt() / 1000).toDouble()/60)
        song = MediaPlayer.create(applicationContext, Uri.parse(last.path))
        song!!.start()
        song!!.isLooping = false
        song!!.setOnCompletionListener(this)
    }

    private fun listChecker(list:ArrayList<MusicData>){
        if (binding!!.everyMusicHandler.visibility == View.VISIBLE) {
            binding!!.PLayingEvery.setOnClickListener {
                if (song!!.isPlaying) {
                    song!!.pause()
                    val now = musicEveryList[positionMusic]
                    NotificationChannelEvery(applicationContext).showNotification(now, "Play")
                    binding!!.PLayingEvery.setImageResource(R.drawable.play)
                } else {
                    song!!.start()
                    val now = musicEveryList[positionMusic]
                    binding!!.PLayingEvery.setImageResource(R.drawable.pause)
                    NotificationChannelEvery(applicationContext).showNotification(now, "Pause")
                }
            }

            binding!!.NextSongEvery.setOnClickListener {
                song!!.reset()
                if (list.size - 1 > positionMusic) {
                    positionMusic += 1
                    next = list[positionMusic]
                    NotificationChannelEvery(applicationContext).showNotification(next!!, "Pause")
                    setResourceForNextSong(next!!)
                } else if (list.size - 1 == positionMusic) {
                    positionMusic = 0
                    next = list[positionMusic]
                    NotificationChannelEvery(applicationContext).showNotification(next!!, "Pause")
                    setResourceForNextSong(next!!)
                }
                if (song!!.isPlaying) {
                    NotificationChannelEvery(applicationContext).showNotification(next!!, "Pause")
                    binding!!.PLayingEvery.setImageResource(R.drawable.pause)
                } else {
                    binding!!.PLayingEvery.setImageResource(R.drawable.play)
                    NotificationChannelEvery(applicationContext).showNotification(next!!, "Play")
                }

            }

            binding!!.PreviousSongEvery.setOnClickListener {
                song!!.reset()
                if (positionMusic > 0) {
                    positionMusic -= 1
                    last = list[positionMusic]
                    NotificationChannelEvery(applicationContext).showNotification(last!!, "Pause")
                    setResourceLastMusic(last!!)

                } else if (positionMusic == 0) {
                    positionMusic = list.size - 1
                    last = list[positionMusic]
                    NotificationChannelEvery(applicationContext).showNotification(last!!, "Pause")
                    setResourceLastMusic(last!!)
                }
                if (song!!.isPlaying) {
                    binding!!.PLayingEvery.setImageResource(R.drawable.pause)
                    NotificationChannelEvery(applicationContext).showNotification(last!!, "Pause")
                } else {
                    binding!!.PLayingEvery.setImageResource(R.drawable.play)
                    NotificationChannelEvery(applicationContext).showNotification(last!!, "Play")

                }
            }
        }
        else if (binding!!.everyMusicSearchedHandler.visibility == View.VISIBLE) {

            binding!!.PLayingEvery.setOnClickListener {
                if (song!!.isPlaying) {
                    song!!.pause()
                    val now = musicEverySearchList[positionMusic]
                    NotificationChannelEverySearch(applicationContext).showNotification(now, "Play")
                    binding!!.PLayingEvery.setImageResource(R.drawable.play)
                } else {
                    song!!.start()
                    val now = musicEverySearchList[positionMusic]
                    binding!!.PLayingEvery.setImageResource(R.drawable.pause)
                    NotificationChannelEverySearch(applicationContext).showNotification(now, "Pause")
                }
            }

            binding!!.NextSongEvery.setOnClickListener {
                song!!.reset()
                if (list.size - 1 > positionMusic) {
                    positionMusic += 1
                    next = list[positionMusic]
                    NotificationChannelEverySearch(applicationContext).showNotification(next!!, "Pause")
                    setResourceForNextSong(next!!)
                } else if (list.size - 1 == positionMusic) {
                    positionMusic = 0
                    next = list[positionMusic]
                    NotificationChannelEverySearch(applicationContext).showNotification(next!!, "Pause")
                    setResourceForNextSong(next!!)
                }
                if (song!!.isPlaying) {
                    NotificationChannelEverySearch(applicationContext).showNotification(next!!, "Pause")
                    binding!!.PLayingEvery.setImageResource(R.drawable.pause)
                } else {
                    binding!!.PLayingEvery.setImageResource(R.drawable.play)
                    NotificationChannelEverySearch(applicationContext).showNotification(next!!, "Play")
                }

            }

            binding!!.PreviousSongEvery.setOnClickListener {
                song!!.reset()
                if (positionMusic > 0) {
                    positionMusic -= 1
                    last = list[positionMusic]
                    NotificationChannelEverySearch(applicationContext).showNotification(last!!, "Pause")
                    setResourceLastMusic(last!!)

                } else if (positionMusic == 0) {
                    positionMusic = list.size - 1
                    last = list[positionMusic]
                    NotificationChannelEverySearch(applicationContext).showNotification(last!!, "Pause")
                    setResourceLastMusic(last!!)
                }
                if (song!!.isPlaying) {
                    binding!!.PLayingEvery.setImageResource(R.drawable.pause)
                    NotificationChannelEverySearch(applicationContext).showNotification(last!!, "Pause")
                } else {
                    binding!!.PLayingEvery.setImageResource(R.drawable.play)
                    NotificationChannelEverySearch(applicationContext).showNotification(last!!, "Play")
                }
            }
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        var list = ArrayList<MusicData>()
        if (binding!!.everyMusicHandler.visibility == View.VISIBLE){
            list = musicEveryList
            if(list.size -1   > positionMusic) {
                positionMusic+=1
                next = list[positionMusic]
                NotificationChannelEvery(applicationContext).showNotification(next!!,"Pause")
                setResourceForNextSong(next!!)
            }else if(list.size -1 == positionMusic) {
                positionMusic = 0
                next = list[positionMusic]
                NotificationChannelEvery(applicationContext).showNotification(next!!,"Pause")
                setResourceForNextSong(next!!)
            }
            if (song!!.isPlaying){
                binding!!.PLayingEvery.setImageResource(R.drawable.pause)
                NotificationChannelEvery(applicationContext).showNotification(next!!,"Pause")
            }
            else{
                binding!!.PLayingEvery.setImageResource(R.drawable.play)
                NotificationChannelEvery(applicationContext).showNotification(next!!,"Play")
            }
        }
        else if (binding!!.everyMusicSearchedHandler.visibility == View.VISIBLE) {
            list = musicEverySearchList
            if (list.size - 1 > positionMusic) {
                positionMusic += 1
                next = list[positionMusic]
                NotificationChannelEverySearch(applicationContext).showNotification(next!!, "Pause")
                setResourceForNextSong(next!!)
            } else if (list.size - 1 == positionMusic) {
                positionMusic = 0
                next = list[positionMusic]
                NotificationChannelEverySearch(applicationContext).showNotification(next!!, "Pause")
                setResourceForNextSong(next!!)
            }
            if (song!!.isPlaying) {
                binding!!.PLayingEvery.setImageResource(R.drawable.pause)
                NotificationChannelEverySearch(applicationContext).showNotification(next!!, "Pause")
            } else {
                binding!!.PLayingEvery.setImageResource(R.drawable.play)
                NotificationChannelEverySearch(applicationContext).showNotification(next!!, "Play")
            }
        }
    }

}