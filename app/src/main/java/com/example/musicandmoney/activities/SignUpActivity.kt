package com.example.musicandmoney.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.musicandmoney.R
import com.example.musicandmoney.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private var userName : TextView ?= null
    private var binding : ActivitySignUpBinding ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        userName = findViewById(R.id.userName)
        binding!!.SignInConfirm.setOnClickListener {
            userCreate()
        }
        setUpActionBar()
        binding!!.toolbarSignUp.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setUpActionBar(){
        setSupportActionBar(binding!!.toolbarSignUp)
        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.back)
    }

    private fun userCreate() {
        val email: String = binding!!.SignEmail.text.toString().trim { it <= ' ' }
        val password: String = binding!!.SignPassword.text.toString().trim { it <= ' ' }
        if (checked(email, password)) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "You have been signed in.", Toast.LENGTH_SHORT).show()
                        val intent  = Intent(this,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
    private fun checked(email:String, password:String):Boolean{
        var pass = false
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
        } else {
            pass = true
        }
        return pass
    }
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}