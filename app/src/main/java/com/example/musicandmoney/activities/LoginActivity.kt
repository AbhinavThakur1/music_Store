package com.example.musicandmoney.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.musicandmoney.R
import com.example.musicandmoney.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private var binding : ActivityLoginBinding?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding!!.SignInLayoutPass.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
            finish()
        }

        binding!!.LogInConfirm.setOnClickListener {
            loginUser()
        }
        setUpActionBar()
        binding!!.toolbarLogin.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setUpActionBar(){
        setSupportActionBar(binding!!.toolbarLogin)
        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.back)
    }

    private fun loginUser() {
        val email = binding!!.logEmail.text.toString().trim { it <= ' ' }
        val password = binding!!.logPassword.text.toString().trim { it <= ' ' }
        if (checked(email, password)) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Log in success", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
    private fun checked(email:String, password:String):Boolean{
        var pass = false
        if (email.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Please enter your email", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Please enter your password", Toast.LENGTH_SHORT).show()
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