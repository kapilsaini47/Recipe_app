package com.dating.recepieapp.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.dating.recepieapp.R
import com.dating.recepieapp.adapter.HomeRecipeAdapter
import com.dating.recepieapp.databinding.FragmentSearchBinding
import com.dating.recepieapp.models.RecipeModel
import com.dating.recepieapp.ui.activities.RecipePage
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SearchFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request focus on the SearchView
        binding.sv.requestFocus()

        // Optionally show the keyboard immediately
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.sv, InputMethodManager.SHOW_IMPLICIT)

    }

    private var _binding:FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeRecipeAdapter: HomeRecipeAdapter
    private lateinit var fireStoreRef:FirebaseFirestore
    private var searchQuery:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)


        fireStoreRef = FirebaseFirestore.getInstance()

        if (searchQuery == null){
            setUpRecipeRecyclerView()
        }

        binding.sv.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchQuery = it
                    searchRecipe(it)
                }
                homeRecipeAdapter.notifyDataSetChanged()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })

        if (::homeRecipeAdapter.isInitialized){
            homeRecipeAdapter.setOnItemClickListener(object : HomeRecipeAdapter.OnItemClickListener{
                override fun onItemClick(recipeModel: RecipeModel) {
                    val recipeIntent = Intent(requireContext(), RecipePage::class.java)
                    recipeIntent.putExtra("recipe_name",recipeModel.title)
                    recipeIntent.putExtra("recipe_image_url",recipeModel.imageUrl)
                    recipeIntent.putExtra("recipe_recipe",recipeModel.recipe)
                    recipeIntent.putExtra("recipe_mode",recipeModel.mode)
                    recipeIntent.putExtra("recipe_id",recipeModel.postId)
                    recipeIntent.putExtra("recipe_cat_id",recipeModel.categoryId)
                    startActivity(recipeIntent)
                }
            })
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

//    override fun onStart() {
//        super.onStart()
//
//        homeRecipeAdapter.startListening()
//    }

    override fun onStop() {
        super.onStop()

        homeRecipeAdapter.stopListening()
    }
    private fun setUpRecipeRecyclerView(){
        val query: Query = fireStoreRef.collection("PublicRecipe")

        val options: FirestoreRecyclerOptions<RecipeModel> = FirestoreRecyclerOptions.Builder<RecipeModel>()
            .setQuery(query, RecipeModel::class.java).build()
        homeRecipeAdapter = HomeRecipeAdapter(options)

        val layoutManager = GridLayoutManager(requireContext(),2)

        binding.rvSearch.apply {
            this.adapter = homeRecipeAdapter
            this.layoutManager = layoutManager
        }
        homeRecipeAdapter.startListening()
    }

    private fun searchRecipe(submittedQuery: String){
        val query: Query = fireStoreRef.collection("PublicRecipe").orderBy("title")
            .startAt(submittedQuery).endAt(submittedQuery+"\uf8ff")

        val options: FirestoreRecyclerOptions<RecipeModel> = FirestoreRecyclerOptions.Builder<RecipeModel>()
            .setQuery(query, RecipeModel::class.java).build()
        homeRecipeAdapter.updateOptions(options)

        binding.rvSearch.apply {
            this.adapter = homeRecipeAdapter

        }
        homeRecipeAdapter.startListening()

    }

}