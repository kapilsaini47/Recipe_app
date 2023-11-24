package com.dating.recepieapp.repository

interface Repository {

    suspend fun signInWithEmail(email:String,password:String):String
}