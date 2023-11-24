package com.dating.recepieapp.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.dating.recepieapp.MainActivity
import com.dating.recepieapp.R
import com.dating.recepieapp.databinding.ActivityAuthBinding
import com.dating.recepieapp.databinding.ActivityWelcomeScreenBinding
import com.dating.recepieapp.helper.AlertDialogBox
import com.dating.recepieapp.helper.NetworkConnection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

class AuthActivity : AppCompatActivity() {

    private var _binding: ActivityAuthBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth :FirebaseAuth
    private lateinit var networkConnection: NetworkConnection
    private lateinit var alertDialogBox : AlertDialogBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_RecepieApp)
        _binding = DataBindingUtil.setContentView(this,R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()
        networkConnection = NetworkConnection()
        alertDialogBox = AlertDialogBox()

        binding.btnLogin.setOnClickListener{
            if (networkConnection.hasInternetConnection(this)){
                if (checkInput()) {
                    alertDialogBox.showProgress(this,this)
                    loginWithEmail()
                }
            }else{
                alertDialogBox.noInternetAlert(this)
            }
        }

        binding.textviewReferSignUp.setOnClickListener{
            val intent = Intent(this@AuthActivity,SignUp::class.java)
            startActivity(intent)
        }

        binding.ivBackNewRecipe.setOnClickListener {
            finish()
        }

    }

    override fun onStart() {
        super.onStart()
        if (networkConnection.hasInternetConnection(this)){
            // Check if user is signed in (non-null) and update UI accordingly.
            val currentUser = auth.currentUser
            if (currentUser != null) {
                updateUi()
            }
        }else{
            alertDialogBox.noInternetAlert(this)
        }
    }

    private fun checkInput():Boolean{
        val email = binding.etEmail.text
        val password = binding.etPassword.text

        if (email.isNullOrEmpty()){
            binding.etEmailIdLogin.helperText = "required"
            binding.etEmailIdLogin.setHelperTextColor(this.getColorStateList(R.color.red))
            return false
        }else if (password.isNullOrEmpty()){
            binding.etPasswordLogin.helperText = "required"
            binding.etPasswordLogin.setHelperTextColor(this.getColorStateList(R.color.red))
            return false
        }
        return true
    }

    private fun loginWithEmail(){
        val email = binding.etEmail.text
        val password = binding.etPassword.text

            auth.signInWithEmailAndPassword(email.toString(),password.toString())
                .addOnCompleteListener(this) { task->
                if (task.isComplete){
                    updateUi()
                }
            }
                .addOnFailureListener{
                    Toast.makeText(this,it.message.toString(),Toast.LENGTH_LONG).show()
                }
    }

    private fun updateUi(){
        if (networkConnection.hasInternetConnection(this)){
            // Check if user is signed in (non-null) and update UI accordingly.
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }else{
            alertDialogBox.noInternetAlert(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }


}