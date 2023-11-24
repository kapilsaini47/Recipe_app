package com.dating.recepieapp.ui.fragment

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.dating.recepieapp.R
import com.dating.recepieapp.adapter.FavoritesAdaptor
import com.dating.recepieapp.databinding.FragmentFavorite2Binding
import com.dating.recepieapp.helper.AlertDialogBox
import com.dating.recepieapp.helper.NetworkConnection
import com.dating.recepieapp.models.FavoriteRecipeModel
import com.dating.recepieapp.ui.activities.RecipePage
import com.dating.recepieapp.viewModel.RecipeViewModel
import com.dating.recepieapp.viewModel.RecipeViewmodelFactory
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Favorite : Fragment() {

    private var _binding:FragmentFavorite2Binding? = null
    private val binding get() = _binding!!

    private var firebaseFirestore:FirebaseFirestore? = null
    private var uid:String? = null
    private lateinit var favoritesAdaptor: FavoritesAdaptor
    private lateinit var viewModel:RecipeViewModel
    private var dialog:AlertDialogBox? = null
    private var networkConnection:NetworkConnection? = null
    private var recipeId:String? = null
    private var favorites :List<FavoriteRecipeModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_favorite2,container,false)

        firebaseFirestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        dialog = AlertDialogBox()
        networkConnection = NetworkConnection()

        val factory = RecipeViewmodelFactory(networkConnection!!,dialog!!, requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[RecipeViewModel::class.java]

       //Notify user about no recipe is set as favorite
        viewModel.likeStatus.observe(viewLifecycleOwner){
            if (it.isNullOrEmpty()){
                binding.ivEmptyFavorites.visibility = View.VISIBLE
                binding.tvEmptyFavorites.visibility = View.VISIBLE
            }else{
                binding.ivEmptyFavorites.visibility = View.GONE
                binding.tvEmptyFavorites.visibility = View.GONE
            }
        }

        setUpRecyclerView()

        //On click to remove and add to favorites at same place
        favoritesAdaptor.setOnFavoriteClickListener(object :FavoritesAdaptor.OnFavoriteClickListener{
            override fun onItemClick(position: Int, favoriteRecipeModel: FavoriteRecipeModel) {
                toggleFavorites(favoriteRecipeModel)

                favoritesAdaptor.notifyItemChanged(position)
            }

        })



        //On click share recipe
        favoritesAdaptor.setOnShareClickListener(object :FavoritesAdaptor.OnShareClickListener{
            override fun onItemClick(position: Int, favoriteRecipeModel: FavoriteRecipeModel) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, favoriteRecipeModel.recipe)
                startActivity(Intent.createChooser(intent,"Share via"))
            }
        })

        //Item click listener
        favoritesAdaptor.setOnItemClickListener(object : FavoritesAdaptor.OnItemClickListener{
            override fun onItemClick(favoriteRecipeModel: FavoriteRecipeModel) {
                val recipeIntent = Intent(requireActivity(),RecipePage::class.java)
                recipeIntent.putExtra("recipe_name",favoriteRecipeModel.title).toString()
                 recipeIntent.putExtra("recipe_image_url",favoriteRecipeModel.imageUrl).toString()
               recipeIntent.putExtra("recipe_recipe",favoriteRecipeModel.recipe).toString()
                recipeIntent.putExtra("recipe_mode",favoriteRecipeModel.mode).toString()
                recipeIntent.putExtra("recipe_mode",favoriteRecipeModel.ratings).toString()
               recipeIntent.putExtra("recipe_id",favoriteRecipeModel.postId).toString()
                recipeIntent.putExtra("recipe_cat_id",favoriteRecipeModel.recipeCatId).toString()
               recipeIntent.putExtra("recipe_fav_id",favoriteRecipeModel.favoriteId).toString()
                startActivity(recipeIntent)
            }

        })

        return binding.root
    }

    private fun setUpRecyclerView(){
        val query: Query = firebaseFirestore?.collection("User")?.document(uid!!)
            ?.collection("Favorites") as Query
        val options: FirestoreRecyclerOptions<FavoriteRecipeModel> = FirestoreRecyclerOptions.Builder<FavoriteRecipeModel>()
            .setQuery(query, FavoriteRecipeModel::class.java).build()

        favoritesAdaptor = FavoritesAdaptor(options)
        val layoutManager = GridLayoutManager(requireContext(), 2)

        binding.rvFavoriteRecipe.apply {
            this.adapter = favoritesAdaptor
            this.layoutManager = layoutManager
        }
        favoritesAdaptor.startListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog = null
        networkConnection = null
        favoritesAdaptor.stopListening()

        _binding = null
    }

    private fun toggleFavorites(favoriteRecipeModel: FavoriteRecipeModel){

        /*
            calling view model method for toggle like button get data of favorites
            then observing all recipes
            then applying logic to get to know if this recipe is set as favorite or not

         */
        viewModel.toggleLikeButton()

        viewModel.likeStatus.observe(this){response->
            favorites = response
        }

        if (favorites?.any{
                it.postId == recipeId
            } == true){
            viewModel.deleteQuoteFromFirebase(favoriteRecipeModel.favoriteId)
            Snackbar.make(requireView(),"Removed from favorites",Snackbar.LENGTH_LONG).apply {
                setAction("Undo"){
                viewModel.addQuoteToFirebase(favoriteRecipeModel)
            }.show()
            }
        }

    }
}