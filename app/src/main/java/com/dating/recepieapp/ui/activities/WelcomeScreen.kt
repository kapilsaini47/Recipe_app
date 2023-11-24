package com.dating.recepieapp.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.dating.recepieapp.MainActivity
import com.dating.recepieapp.R
import com.dating.recepieapp.databinding.ActivityWelcomeScreenBinding
import com.dating.recepieapp.helper.AlertDialogBox
import com.dating.recepieapp.helper.NetworkConnection
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

class WelcomeScreen : AppCompatActivity() {

    private var _binding:ActivityWelcomeScreenBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth:FirebaseAuth
    private lateinit var networkConnection : NetworkConnection
    private lateinit var alertDialogBox :AlertDialogBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_RecepieApp)
        _binding = DataBindingUtil.setContentView(this,R.layout.activity_welcome_screen)

        auth = FirebaseAuth.getInstance()
        networkConnection = NetworkConnection()
        alertDialogBox = AlertDialogBox()

        binding.tvLogin.setOnClickListener{
            val intent = Intent(this,AuthActivity::class.java)
            startActivity(intent)
        }

        binding.cvSignUp.setOnClickListener{
            val intent = Intent(this,SignUp::class.java)
            startActivity(intent)
        }


    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    override fun onStart() {
        super.onStart()
        if (networkConnection.hasInternetConnection(this)){
            // Check if user is signed in (non-null) and update UI accordingly.
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val intent = Intent(this,MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }else{
            alertDialogBox.noInternetAlert(this)
        }
    }
}