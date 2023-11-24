package com.dating.recepieapp.models

data class FavoriteRecipeModel @JvmOverloads constructor(
    val favoriteId:String = "",
    val postId: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val recipe:String = "",
    val ratings:String = "",
    val mode:String = "",
    val recipeCatId:String = ""

)