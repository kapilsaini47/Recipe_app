package com.dating.recepieapp.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.dating.recepieapp.MainActivity
import com.dating.recepieapp.R
import dagger.hilt.android.AndroidEntryPoint

class PopUpPage : AppCompatActivity() {

    private lateinit var btnHome:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_RecepieApp)
        setContentView(R.layout.activity_pop_up_page)

        btnHome = findViewById(R.id.btnBackToHomePage)

        btnHome.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }


}