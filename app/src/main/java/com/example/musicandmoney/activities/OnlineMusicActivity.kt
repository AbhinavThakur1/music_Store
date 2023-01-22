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
import com.example.musicandmoney.adapter.MusicOnlineAdapter
import com.example.musicandmoney.databinding.ActivityOnlineMusicBinding
import com.example.musicandmoney.model.MusicData
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class OnlineMusicActivity : AppCompatActivity() , MediaPlayer.OnCompletionListener{
    companion object{
        var song : MediaPlayer?=null
        var positionMusic: Int = 0
        var musicOnlineList = ArrayList<MusicData>()
        @SuppressLint("StaticFieldLeak")
        var binding : ActivityOnlineMusicBinding ?=null
    }
    private var next: MusicData? = null
    private var last: MusicData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineMusicBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setUpActionBar()
        checkIfUserIsLoggedIn()
        appCheck()
        fireStoreGetData()
        binding!!.toolbarOnlineMusic.setNavigationOnClickListener {
            onBackPressed()
            song = null
        }
        binding!!.MusicSeekBarOnline.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) song?.seekTo(progress)
                binding!!.MusicTimeCounterOnline.text = String.format("%.2f",(seekBar!!.progress / 1000).toDouble()/60)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

    }
    private fun setUpActionBar(){
        setSupportActionBar(binding!!.toolbarOnlineMusic)
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

    private fun fireStoreGetData(){
        FirebaseFirestore.getInstance().collection(checkIfUserIsLoggedIn()).get()
            .addOnSuccessListener{
                    it->
                val list :List<DocumentSnapshot> =it.documents
                for(doc in list){
                    val path =doc.data!!.getValue("path").toString()
                    val title =doc.data!!.getValue("title").toString()
                    val duration =doc.data!!.getValue("duration").toString()
                    val artistName =doc.data!!.getValue("artistName").toString()
                    val filename =doc.data!!.getValue("fileName").toString()
                    val combine = MusicData(path, title, duration, artistName, filename)
                    musicOnlineList.add(combine)
                }
                setUpOnlineRecycleView(musicOnlineList)
                if (musicOnlineList.isNotEmpty()){
                    binding!!.OnlineMusicHandler .visibility = View.VISIBLE
                    binding!!.NoData.visibility = View.GONE
                    listChecker(musicOnlineList)
                }else{
                    binding!!.OnlineMusicHandler.visibility = View.GONE
                    binding!!.NoData.visibility = View.VISIBLE
                }
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        musicOnlineList = ArrayList<MusicData>()
        super.onBackPressed()
        if (song!=null) {
            song!!.stop()
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }
    }

    private fun setUpOnlineRecycleView(list : ArrayList<MusicData>){
        val adapter =  MusicOnlineAdapter(this,list)
        val recycleView = binding!!.OnlineMusicHandler
        recycleView.adapter = adapter
        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(this)
        adapter.setOnclickListener(object : MusicOnlineAdapter.OnClickListener{
            override fun onclick(position: Int, model: MusicData) {
                if(song!= null){
                    song!!.reset()
                }
                NotificationChannelOnline(applicationContext).showNotification(model,"Pause")
                song = MediaPlayer.create(applicationContext,Uri.parse(model.path))
                song!!.start()
                song!!.isLooping = false
                song!!.setOnCompletionListener(this@OnlineMusicActivity)
                positionMusic = position
                binding!!.PLayingOnline.setImageResource(R.drawable.pause)
                binding!!.OnlineMusicPlayName.text = model.title
                binding!!.MusicSeekBarOnline.max = model.duration.toInt()
                Handler().postDelayed(object: Runnable{
                    override fun run() {
                        try {
                            binding!!.MusicSeekBarOnline.progress = song!!.currentPosition
                            Handler().postDelayed(this,1000)
                        }catch (e:Exception){
                            binding!!.MusicSeekBarOnline.progress = 0
                        }
                    }
                },0)
                binding!!.MusicEndTimeOnline.text= String.format("%.2f",(model.duration.toInt() / 1000).toDouble()/60)
                binding!!.CardViewOnlineMusic.visibility = View.VISIBLE
            }
        })
        adapter.setOnLongClickListener(object : MusicOnlineAdapter.OnLongClickListener{
            override fun onLongClick(position: Int, model: MusicData) {
                AlertDialog.Builder(this@OnlineMusicActivity).setTitle("Local files cannot be deleted but online can.")
                    .setPositiveButton("yes"){
                            dialog,_ ->
                        FirebaseFirestore.getInstance()
                            .collection(checkIfUserIsLoggedIn())
                            .document(model.fileName).delete()
                        adapter.notifyItemRemoved(position)
                    }.setNegativeButton("No"){
                            dialog,_ ->
                        dialog.dismiss()
                    }.show()
            }
        })
    }

    private fun setResourceForNextSong(next :MusicData){
        binding!!.MusicSeekBarOnline.progress = 0
        binding!!.OnlineMusicPlayName .text = next.title
        binding!!.MusicSeekBarOnline.max = next.duration.toInt()
        Handler().postDelayed(object: Runnable{
            override fun run() {
                try {
                    binding!!.MusicSeekBarOnline.progress = song!!.currentPosition
                    Handler().postDelayed(this,1000)
                }catch (e:Exception){
                    binding!!.MusicSeekBarOnline.progress = 0
                }
            }
        },0)
        binding!!.MusicEndTimeOnline.text = String.format("%.2f",(next.duration.toInt() / 1000).toDouble()/60)
        song = MediaPlayer.create(applicationContext, Uri.parse(next.path))
        song!!.start()
        song!!.isLooping = false
        song!!.setOnCompletionListener(this)
    }

    private fun setResourceLastMusic(last : MusicData){
        binding!!.MusicSeekBarOnline.progress = 0
        binding!!.OnlineMusicPlayName.text = last.title
        binding!!.MusicSeekBarOnline.max = last.duration.toInt()
        Handler().postDelayed(object: Runnable{
            override fun run() {
                try {
                    binding!!.MusicSeekBarOnline.progress = song!!.currentPosition
                    Handler().postDelayed(this,1000)
                }catch (e:Exception){
                    binding!!.MusicSeekBarOnline.progress = 0
                }
            }
        },0)
        binding!!.MusicEndTimeOnline.text = String.format("%.2f",(last.duration.toInt() / 1000).toDouble()/60)
        song = MediaPlayer.create(applicationContext, Uri.parse(last.path))
        song!!.start()
        song!!.isLooping = false
        song!!.setOnCompletionListener(this)
    }

    private fun listChecker(list:ArrayList<MusicData>){

        binding!!.PLayingOnline.setOnClickListener {
            if (song!!.isPlaying){
                song!!.pause()
                val now =musicOnlineList[positionMusic]
                NotificationChannelOnline(applicationContext).showNotification(now,"Play")
                binding!!.PLayingOnline.setImageResource(R.drawable.play)
            }
            else{song!!.start()
                val now =musicOnlineList[positionMusic]
                NotificationChannelOnline(applicationContext).showNotification(now,"Pause")
                binding!!.PLayingOnline.setImageResource(R.drawable.pause)
            }
        }


        binding!!.NextSongOnline.setOnClickListener {
            song!!.reset()
            if(list.size -1   > positionMusic) {
                positionMusic+=1
                next = list[positionMusic]
                NotificationChannelOnline(applicationContext).showNotification(next!!,"Pause")
                setResourceForNextSong(next!!)
            }else if(list.size -1 == positionMusic) {
                positionMusic = 0
                next = list[positionMusic]
                NotificationChannelOnline(applicationContext).showNotification(next!!,"Pause")
                setResourceForNextSong(next!!)
            }
            if (song!!.isPlaying){
                binding!!.PLayingOnline.setImageResource(R.drawable.pause)
                NotificationChannelOnline(applicationContext).showNotification(next!!,"Pause")
            }
            else{
                binding!!.PLayingOnline.setImageResource(R.drawable.play)
                NotificationChannelOnline(applicationContext).showNotification(next!!,"Play")
            }

        }

        binding!!.PreviousSongOnline.setOnClickListener {
            song!!.reset()
            if (positionMusic  > 0) {
                positionMusic-=1
                last = list[positionMusic]
                NotificationChannelOnline(applicationContext).showNotification(last!!,"Pause")
                setResourceLastMusic(last!!)

            }else if (positionMusic == 0){
                positionMusic = list.size -1
                last = list[positionMusic]
                NotificationChannelOnline(applicationContext).showNotification(last!!,"Pause")
                setResourceLastMusic(last!!)
            }
            if (song!!.isPlaying){
                NotificationChannelOnline(applicationContext).showNotification(last!!,"Pause")
                binding!!.PLayingOnline.setImageResource(R.drawable.pause)
            }
            else{
                binding!!.PLayingOnline.setImageResource(R.drawable.play)
                NotificationChannelOnline(applicationContext).showNotification(last!!,"Play")
            }
        }
    }
    override fun onCompletion(mp: MediaPlayer?) {
        val list = musicOnlineList
        if(list.size -1   > positionMusic) {
            positionMusic+=1
            next = list[positionMusic]
            NotificationChannelOnline(applicationContext).showNotification(next!!,"Pause")
            setResourceForNextSong(next!!)
        }else if(list.size -1 == positionMusic) {
            positionMusic = 0
            next = list[positionMusic]
            NotificationChannelOnline(applicationContext).showNotification(next!!,"Pause")
            setResourceForNextSong(next!!)
        }
        if (song!!.isPlaying){
            NotificationChannelOnline(applicationContext).showNotification(next!!,"Pause")
            binding!!.PLayingOnline.setImageResource(R.drawable.pause)
        }
        else{
            binding!!.PLayingOnline.setImageResource(R.drawable.play)
            NotificationChannelOnline(applicationContext).showNotification(next!!,"Play")
        }
    }
}