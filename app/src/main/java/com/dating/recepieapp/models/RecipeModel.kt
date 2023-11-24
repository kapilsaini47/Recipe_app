package com.dating.recepieapp.models

data class RecipeModel @JvmOverloads constructor(
    val postId: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val recipe:String = "",
    val ratings:String = "",
    val userId:String = "",
    val categoryId:String = "",
    val mode:String = ""
)
