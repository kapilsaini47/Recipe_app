package com.dating.recepieapp.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.dating.recepieapp.MainActivity
import com.dating.recepieapp.R
import com.dating.recepieapp.databinding.ActivityAuthBinding
import com.dating.recepieapp.databinding.ActivitySignUpBinding
import com.dating.recepieapp.helper.AlertDialogBox
import com.dating.recepieapp.helper.NetworkConnection
import com.dating.recepieapp.models.UserModel
import com.dating.recepieapp.viewModel.RecipeViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUp : AppCompatActivity() {

    private var _binding: ActivitySignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth:FirebaseAuth
    private lateinit var dialog: AlertDialogBox
    private lateinit var networkConnection :NetworkConnection
    private lateinit var fireStoreDbRef:FirebaseFirestore
    private var userProfileGeneratedId:String? = null
    //private val viewModel by viewModels<RecipeViewModel>()

    private val TAG = "SignUp Activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_RecepieApp)
        _binding = DataBindingUtil.setContentView(this,R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        networkConnection = NetworkConnection()
        dialog = AlertDialogBox()
        fireStoreDbRef = FirebaseFirestore.getInstance()
        userProfileGeneratedId = fireStoreDbRef.collection("User").document().id

        binding.btnSignup.setOnClickListener {
            if (networkConnection.hasInternetConnection(this)){
                if (checkTextFields()){
                    dialog.showProgress(this,this)
                    createAccountWithEmail()
                }
            }else{
                //alertDialogBox.showNoInternetDialog(requireContext())
                Snackbar.make(View(this),"No Internet",Snackbar.LENGTH_LONG).show()
            }

        }

        binding.textviewReferLogin.setOnClickListener{
            val intent = Intent(this,AuthActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onStart() {
        super.onStart()
        if (networkConnection.hasInternetConnection(this)){
            // Check if user is signed in (non-null) and update UI accordingly.
            val currentUser = auth.currentUser
            if (currentUser != null) {
                updateUI()
            }
        }else{
            dialog.noInternetAlert(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    private fun createAccountWithEmail() {

        val email = binding.etSignupEmail.text.toString()
        val password = binding.etSignupRepeatPassword.text.toString()

        CoroutineScope(Dispatchers.IO).launch {

            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this@SignUp) { task ->
                        addUserDetails()
                    }
                withContext(Dispatchers.Main){
                    Log.d(TAG, "createUserWithEmail:success")

                }
            }catch (e:Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@SignUp,e.message.toString(),Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun addUserDetails(){

        val uid = auth.currentUser?.uid.toString()
        val name = binding.etSignupName.text.toString()
        val userImage = ""
        val userData = UserModel(uid,uid,name,userImage)
        if (checkTextFields()){
            CoroutineScope(Dispatchers.IO).launch {

                try {
                    fireStoreDbRef.collection("User").document(uid)
                        .collection("UserDetails").document(uid)
                        .set(userData)
                    withContext(Dispatchers.Main){
                        updateUI()
                    }

                }catch (e:Exception){
                    Toast.makeText(this@SignUp,e.message.toString(),Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    
    private fun updateUI() {
        val intent = Intent(this,MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun checkTextFields():Boolean{
        val name = binding.etSignupName.text
        val email = binding.etSignupEmail.text
        val password = binding.etSignupPassword.text
        val confirmPassword = binding.etSignupRepeatPassword.text

        if (email.isNullOrEmpty()){
            binding.etEmailIdSignup.helperText = "required"
            binding.etEmailIdSignup.setHelperTextColor(this.getColorStateList(R.color.red))
            return false
        }else if (name.isNullOrEmpty()){
            binding.etNameSignup.helperText = "required"
            binding.etNameSignup.setHelperTextColor(this.getColorStateList(R.color.red))
            return false
        }else if (password.isNullOrEmpty()){
            binding.etPasswordSignup.helperText = "required"
            binding.etPasswordSignup.setHelperTextColor(this.getColorStateList(R.color.red))
            return false
        }else if (confirmPassword.isNullOrEmpty()){
            binding.etRePasswordSignup.helperText = "required"
            binding.etRePasswordSignup.setHelperTextColor(this.getColorStateList(R.color.red))
            return false
        }else if (confirmPassword.toString() != password.toString()){
            binding.etPasswordSignup.helperText = "required"
            binding.etRePasswordSignup.helperText = "required"
            binding.etPasswordSignup.setHelperTextColor(this.getColorStateList(R.color.red))
            binding.etRePasswordSignup.setHelperTextColor(this.getColorStateList(R.color.red))
            Toast.makeText(this,"Password and Confirm password are not same", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }


}