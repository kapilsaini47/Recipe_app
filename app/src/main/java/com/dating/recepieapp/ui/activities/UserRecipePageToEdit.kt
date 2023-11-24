package com.dating.recepieapp.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dating.recepieapp.R
import dagger.hilt.android.AndroidEntryPoint

class UserRecipePageToEdit : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_RecepieApp)
        setContentView(R.layout.activity_user_recipe_page_to_edit)
    }
}