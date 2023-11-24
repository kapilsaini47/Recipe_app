package com.dating.recepieapp.models


data class UserModel @JvmOverloads constructor(
//data class properties with default values so fire-store can deserialize object(
    val dataId:String = "",
    val id:String = "",
    val name:String = "",
    val imageUri:String = ""
)
