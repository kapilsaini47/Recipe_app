package com.dating.recepieapp.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.dating.recepieapp.MainActivity
import com.dating.recepieapp.R
import com.dating.recepieapp.databinding.ActivityAddNewCategoryBinding
import com.dating.recepieapp.helper.AlertDialogBox
import com.dating.recepieapp.helper.NetworkConnection
import com.dating.recepieapp.models.CategoriesModel
import com.dating.recepieapp.repository.Repository
import com.dating.recepieapp.repository.RepositoryImpl
import com.dating.recepieapp.viewModel.RecipeViewModel
import com.dating.recepieapp.viewModel.RecipeViewmodelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AddNewCategory : AppCompatActivity() {

    private var _binding:ActivityAddNewCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var fireStoreDb:FirebaseFirestore
    private lateinit var auth:FirebaseAuth
    private var dialog :AlertDialogBox? = null
    private lateinit var viewModel: RecipeViewModel
    private var networkConnection: NetworkConnection? = null
    private lateinit var generatedId :String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_RecepieApp)
        _binding = DataBindingUtil.setContentView(this,R.layout.activity_add_new_category)

        auth = FirebaseAuth.getInstance()
        fireStoreDb = FirebaseFirestore.getInstance()
        dialog = AlertDialogBox()
        networkConnection = NetworkConnection()

        val factory = RecipeViewmodelFactory(networkConnection!!,dialog!!,application)
        viewModel = ViewModelProvider(this, factory)[RecipeViewModel::class.java]

        generatedId = viewModel.generateId().toString()

        binding.btnCategorySubmit.setOnClickListener {
            if (checkInput()) {
                val name = binding.etCategoryName.text?.trim().toString()
                val categoryData = CategoriesModel(generatedId, name)

                addNewCategoryToFirebase(categoryData)

                binding.btnCategorySubmit.setBackgroundColor(this.getColor(R.color.grey))
                binding.btnCategorySubmit.isClickable = false

            }
        }


    }

    private fun addNewCategoryToFirebase(categoriesModel: CategoriesModel){

        val recipeRef = viewModel.addNewCategory()?.document(generatedId)

        // Use a coroutine to perform Firestore operations on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {

                recipeRef?.set(categoriesModel)?.await()

                addPublicCategory(categoriesModel)
                // Update the UI on the main thread
                withContext(Dispatchers.Main) {
                    dialog.let {
                        it?.showProgress(this@AddNewCategory,this@AddNewCategory)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddNewCategory, e.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun addPublicCategory(categoriesModel: CategoriesModel){
        val ref = viewModel.addPublicCategory()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                ref?.add(categoriesModel)?.await()

                // Update the UI on the main thread
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@AddNewCategory, PopUpPage::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddNewCategory, e.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }

    }




    override fun onDestroy() {
        super.onDestroy()
        dialog = null

        _binding = null

    }

    private fun checkInput():Boolean{
        val categoryName = binding.etCategoryName.text
        if (categoryName.isNullOrEmpty()){
            binding.tiLayout.helperText = "required"
            binding.tiLayout.setHelperTextColor(this.getColorStateList(R.color.red))
            return false
        }
        return true
    }
}