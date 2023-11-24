package com.dating.recepieapp.repository

import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RepositoryImpl:Repository {

    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun signInWithEmail(email: String, password: String):String {
        var status = ""
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
            if (it.isComplete){
                status = "complete"
            }
        }
            .addOnFailureListener(OnFailureListener {
                status = it.message.toString()
            })
        return status
    }


}