package com.dating.recepieapp.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dating.recepieapp.R
import com.dating.recepieapp.databinding.ActivityRecipePageBinding
import com.dating.recepieapp.helper.AlertDialogBox
import com.dating.recepieapp.helper.NetworkConnection
import com.dating.recepieapp.models.FavoriteRecipeModel
import com.dating.recepieapp.models.RecipeModel
import com.dating.recepieapp.models.RequestModel
import com.dating.recepieapp.viewModel.RecipeViewModel
import com.dating.recepieapp.viewModel.RecipeViewmodelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RecipePage : AppCompatActivity() {

    private var _binding:ActivityRecipePageBinding? = null
    private val binding get() = _binding!!

    private var recipeName:String? = null
    private var recipeImageUrl:String? = null
    private var recipeRecipe:String? = null
    private var recipeMode:String? = null
    private var fireStoreDb:FirebaseFirestore? = null
    private lateinit var viewModel: RecipeViewModel
    private var networkConnection: NetworkConnection? = null
    private var uId:String? = null
    private var recipeId:String? = null
    private var recipeCatId:String? = null
    private var dialog:AlertDialogBox? =null
    private var ratings:String? = null
    private var generatedId:String? = null
    private var recipeFavId:String? = null
    private var favorites :List<FavoriteRecipeModel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_RecepieApp)
        _binding = DataBindingUtil.setContentView(this,R.layout.activity_recipe_page)

        fireStoreDb = FirebaseFirestore.getInstance()
        uId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        dialog = AlertDialogBox()
        networkConnection = NetworkConnection()

        val factory = RecipeViewmodelFactory(networkConnection!!,dialog!!,application)
        viewModel = ViewModelProvider(this, factory)[RecipeViewModel::class.java]

        //Use favorite recipe id if it is null then generate id for favorites
        generatedId = viewModel.generateId().toString()

        val intent = intent
        recipeName = intent?.getStringExtra("recipe_name")
        recipeImageUrl = intent?.getStringExtra("recipe_image_url")
        recipeRecipe = intent?.getStringExtra("recipe_recipe")
        recipeMode = intent?.getStringExtra("recipe_mode")
        recipeId = intent?.getStringExtra("recipe_id")
        recipeCatId = intent?.getStringExtra("recipe_cat_id")

        this.title = recipeName

        binding.ivAddFavorite.setOnClickListener {
            toggleFavorites()
        }

        binding.btnSendRequest.setOnClickListener {
            val requestModel = RequestModel(
                generatedId!!,recipeName!!,recipeId!!,uId!!,false
            )
            sendRequest(requestModel)

            binding.btnSendRequest.setBackgroundColor(this.getColor(R.color.grey))
            binding.btnSendRequest.isClickable = false
        }

        binding.ivBackRecipePage.setOnClickListener {
            finish()
        }

        binding.ratingBar.setOnRatingBarChangeListener{ _,rating,_ ->
            ratings = rating.toString()
            if (recipeMode ==  "private"){
                Toast.makeText(this,"Send request to rate.",Toast.LENGTH_LONG).show()
            }else if (recipeMode == "public"){
                Toast.makeText(this,ratings.toString(),Toast.LENGTH_LONG).show()
                val recipeModel = RecipeModel(
                    recipeId!!,recipeName!!,recipeImageUrl!!,recipeRecipe!!,
                    rating.toString(),uId!!, recipeCatId!!,recipeMode!!
                )
                addNewRatings(recipeModel)

                binding.ratingBar.isClickable = false
            }
        }

        setUpData()

    }

    private fun sendRequest(requestModel: RequestModel) {
        viewModel.sendRequestToUser(requestModel)?.document(generatedId!!)?.set(requestModel)
        viewModel.saveSentRequest(requestModel)?.document(generatedId!!)?.set(requestModel)
    }

    private fun setUpData(){
        binding.tvCurrentRecipeName.text = recipeName
        Glide.with(this).load(recipeImageUrl).error(R.drawable.recipe_image).into(binding.ivRecipeImage)
        binding.tvCurrentRecipeRecipe.text = recipeRecipe
        if (recipeFavId.isNullOrEmpty()){
            binding.ivAddFavorite.setImageResource(R.drawable.baseline_favorite)
        }else{
            binding.ivAddFavorite.setImageResource(R.drawable.baseline_favorite_24)
        }

        if (recipeMode == "private"){
            binding.btnSendRequest.visibility = View.VISIBLE
            binding.tvCurrentRecipeRecipe.visibility = View.GONE
            binding.tvPrivateWarning.visibility = View.VISIBLE
        }else if(recipeMode == "public"){
            binding.btnSendRequest.visibility = View.GONE
            binding.tvCurrentRecipeRecipe.visibility = View.VISIBLE
            binding.tvPrivateWarning.visibility = View.GONE
        }

    }

    private fun addNewRatings(recipeModel: RecipeModel){
        val recipeRef = fireStoreDb?.collection("User")?.document(uId!!)
            ?.collection("Recipes")?.document(recipeId!!)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                recipeRef?.set(recipeModel)?.await()
                //finally call public recipe method
                uploadPublicRecipe(recipeModel)

                // Update the UI on the main thread
                withContext(Dispatchers.Main) {

                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RecipePage, e.message.toString(), Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    private fun uploadPublicRecipe(recipeModel: RecipeModel){
        val recipeRef = fireStoreDb?.collection("PublicRecipe")?.document(recipeId!!)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                recipeRef?.set(recipeModel)?.await()

                // Update the UI on the main thread
                withContext(Dispatchers.Main) {
                   Toast.makeText(this@RecipePage,"Ratings Added",Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RecipePage, e.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun toggleFavorites(){

        val recipeModel = FavoriteRecipeModel(
            generatedId!!,recipeId!!,recipeName!!,recipeImageUrl!!,recipeRecipe!!,
            ratings ?: "",recipeMode!!,recipeCatId!!
        )

        viewModel.toggleLikeButton()

        viewModel.likeStatus.observe(this){response->
            favorites = response
        }

        if (favorites?.any{
                it.postId == recipeId.toString()
            } == true){
            viewModel.deleteQuoteFromFirebase(recipeFavId!!)
            binding.ivAddFavorite.setImageResource(R.drawable.baseline_favorite)
            Toast.makeText(this,"Removed from favorites",Toast.LENGTH_SHORT).show()
        }else{
            viewModel.addQuoteToFirebase(recipeModel)
            binding.ivAddFavorite.setImageResource(R.drawable.baseline_favorite_24)
            Toast.makeText(this,"Added to favorites",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recipeImageUrl = null
        recipeName = null
        recipeRecipe = null

        _binding = null
    }
}