package com.dating.recepieapp.models

data class RequestModel @JvmOverloads constructor(
    val requestId:String = "",
    val recipeName:String = "",
    val recipeId:String = "",
    val userId:String = "",
    val isApproved:Boolean = false
)