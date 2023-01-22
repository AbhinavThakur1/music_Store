package com.example.musicandmoney.firebase

import android.content.Context
import android.widget.Toast
import com.example.musicandmoney.model.MusicData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

open class FireBaseClass {
    private val myFireStore = FirebaseFirestore.getInstance()
    fun createCollectionInFireBase(context: Context, musicInfo: MusicData){
        myFireStore.document(getCurrentUserId()).set(musicInfo,SetOptions.merge()).addOnSuccessListener {
            Toast.makeText(context, "All Set", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getCurrentUserId():String{
        var id = ""
        val currentUSerId=FirebaseAuth.getInstance().currentUser
        if(currentUSerId != null){
            id = currentUSerId.uid
        }
        return id
    }
}