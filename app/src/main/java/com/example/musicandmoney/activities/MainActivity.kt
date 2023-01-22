package com.example.musicandmoney.activities

import android.Manifest
import android.content.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore.*
import android.provider.MediaStore.Audio.*
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.musicandmoney.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MainActivity : AppCompatActivity(){
    private var doubleBackPressed = false
    private var binding: ActivityMainBinding? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding!!.logInLayoutPass.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        permissions()
        if (checkIfUserIsLoggedIn().isNotEmpty()){
            binding!!.logInLayoutPass.visibility = View.GONE
            binding!!.logOut.visibility =View.VISIBLE
        }

        binding!!.GoLocalMusic.setOnClickListener{
            startActivity(Intent(this,LocalMusicActivity::class.java))
        }

        binding!!.GoOnlineMusic.setOnClickListener{
            if (checkIfUserIsLoggedIn().isNotEmpty()) {
                startActivity(Intent(this, OnlineMusicActivity::class.java))
            }else{
                Toast.makeText(this, "Please Log In To Activate Online Storage.", Toast.LENGTH_SHORT).show()
            }
        }

        binding!!.GoEveryMusic.setOnClickListener{
            startActivity(Intent(this,EveryMusicActivity::class.java))
        }

        binding!!.logOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            binding!!.logInLayoutPass.visibility = View.VISIBLE
        }

    }


    private fun checkIfUserIsLoggedIn() :  String{
        val currentUser = FirebaseAuth.getInstance().currentUser
        var isLoggedIn = ""
        if(currentUser != null){
            isLoggedIn = currentUser.uid
        }
        return isLoggedIn
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        onDoubleBackPressed()
    }

    private fun onDoubleBackPressed(){
        if(doubleBackPressed){
            super.onBackPressed()
            return
        }
        this.doubleBackPressed = true
        Toast.makeText(this, "Press one more time to exit the App.", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({this.doubleBackPressed = false},3000)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun permissions(){
        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener{
                override fun onPermissionsChecked(Report: MultiplePermissionsReport) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Permissions are denied allow them in application setting.")
                        .setPositiveButton("Yes"){
                                _,_ ->
                            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
                        }
                        .setNegativeButton("Cancel"){
                                dialog,_ ->
                            dialog.dismiss()
                        }.show()
                }

            }).onSameThread().check()

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}