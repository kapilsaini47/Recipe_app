package com.dating.recepieapp.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.dating.recepieapp.R
import com.dating.recepieapp.databinding.ActivityAddNewRecipeBinding
import com.dating.recepieapp.helper.AlertDialogBox
import com.dating.recepieapp.helper.NetworkConnection
import com.dating.recepieapp.models.RecipeModel
import com.dating.recepieapp.utils.Util.PICK_IMAGE_REQUEST
import com.dating.recepieapp.viewModel.RecipeViewModel
import com.dating.recepieapp.viewModel.RecipeViewmodelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AddNewRecipe : AppCompatActivity() {

    private var _binding:ActivityAddNewRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fireStoreDb: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recipeSharingMode: String

    private var filePath: Uri? = null
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference: StorageReference = storage.reference
    private lateinit var imageUrl:String
    private lateinit var categoryId:String
    private var dialog : AlertDialogBox? = null
    private lateinit var viewModel: RecipeViewModel
    private var networkConnection: NetworkConnection? = null
    private lateinit var generatedId :String
    private lateinit var uId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_RecepieApp)
        _binding = DataBindingUtil.setContentView(this,R.layout.activity_add_new_recipe)

        auth = FirebaseAuth.getInstance()
        fireStoreDb = FirebaseFirestore.getInstance()
        uId = auth.currentUser?.uid.toString()
        fireStoreDb = FirebaseFirestore.getInstance()
        dialog = AlertDialogBox()
        networkConnection = NetworkConnection()

        val factory = RecipeViewmodelFactory(networkConnection!!,dialog!!,application)
        viewModel = ViewModelProvider(this, factory)[RecipeViewModel::class.java]

        generatedId = viewModel.generateId().toString()

        binding.ivBackNewRecipe.setOnClickListener {
            finish()
        }

        binding.tvPrivate.setOnClickListener {
            recipeSharingMode = "private"
            binding.tvPrivate.setBackgroundColor(this.getColor(R.color.grey))
            binding.tvPublic.setBackgroundColor(this.getColor(R.color.white))
        }

        binding.tvPublic.setOnClickListener {
            recipeSharingMode = "public"
            binding.tvPrivate.setBackgroundColor(this.getColor(R.color.white))
            binding.tvPublic.setBackgroundColor(this.getColor(R.color.grey))
        }

        binding.ivNewRecipeImage.setOnClickListener {
            chooseImage()
        }

        //receive category id
        val intent = intent
        if (intent != null) {
            val receivedString = intent.getStringExtra("categoryId")
            if (receivedString != null) {
                categoryId = receivedString
            }
        }

        binding.btnSubmitNewRecipe.setOnClickListener {
            uploadImageAndData()
            binding.btnSubmitNewRecipe.setBackgroundColor(this.getColor(R.color.grey))
            binding.btnSubmitNewRecipe.isClickable = false
        }

    }

    private fun addNewRecipe(recipeModel: RecipeModel){
        val recipeRef = fireStoreDb.collection("User").document(uId)
            .collection("Recipes").document(generatedId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                recipeRef.set(recipeModel).await()

                //finally call public recipe method
                uploadPublicRecipe(recipeModel)

                // Update the UI on the main thread
                withContext(Dispatchers.Main) {

                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddNewRecipe, e.message.toString(), Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    private fun uploadPublicRecipe(recipeModel: RecipeModel){
        val recipeRef = fireStoreDb.collection("PublicRecipe").document(generatedId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                recipeRef.set(recipeModel).await()

                // Update the UI on the main thread
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@AddNewRecipe, PopUpPage::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddNewRecipe, e.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun chooseImage(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            PICK_IMAGE_REQUEST
        )
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == PICK_IMAGE_REQUEST) && (resultCode == Activity.RESULT_OK) &&
            (data != null) && (data.data != null)
        ) {
            filePath = data.data
            binding.ivRecipeImage.setImageURI(filePath)
           // imageView.setImageURI(filePath)
        }
    }

    private fun checkInputs():Boolean{
        val recipeMethod = binding.etRecipeMethod.text
        val recipeTitle = binding.etRecipeTitle.text
        if (recipeMethod.isNullOrEmpty()){
            binding.tiLayoutRecipeMethod.helperText = "Required"
            binding.tiLayoutRecipeMethod.setHelperTextColor(this.getColorStateList(R.color.red))
            return false
        }else if (recipeTitle.isNullOrEmpty()){
            binding.tiLayoutRecipeTitle.helperText = "Required"
            binding.tiLayoutRecipeTitle.setHelperTextColor(this.getColorStateList(R.color.red))
            return false
        }
        return true
    }

    //first upload image to get image uri
    private fun uploadImageAndData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val recipeStorageReference = storageReference.child("recipeImages")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val uploadTask = recipeStorageReference.child("$generatedId.jpg").putFile(filePath!!)

                    // Wait for the upload to complete
                    uploadTask.await()

                    // Get the download URL
                    val uri = recipeStorageReference.downloadUrl.await()
                    if (checkInputs()){
                        //get generated id
                        val recipeId = generatedId
                        val recipeMethod = binding.etRecipeMethod.text.toString()
                        val title = binding.etRecipeTitle.text.toString()

                        val recipeModel = RecipeModel(
                            recipeId,title,uri.toString(),recipeMethod,"",uId, categoryId,recipeSharingMode
                        )

                        //second call add new recipe
                        addNewRecipe(recipeModel)
                    }

                    withContext(Dispatchers.Main) {

                        dialog.let {
                        it?.showProgress(this@AddNewRecipe,this@AddNewRecipe)
                    }
                        Toast.makeText(this@AddNewRecipe, "Recipe uploaded successfully", Toast.LENGTH_SHORT).show()
                        //finish()
                    }
                } catch (exception: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddNewRecipe, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}