package com.dating.recepieapp.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintSet.Motion
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.dating.recepieapp.R
import com.dating.recepieapp.adapter.HomeCategoryAdapter
import com.dating.recepieapp.adapter.HomeRecipeAdapter
import com.dating.recepieapp.databinding.FragmentHome2Binding
import com.dating.recepieapp.helper.AlertDialogBox
import com.dating.recepieapp.helper.NetworkConnection
import com.dating.recepieapp.models.RecipeModel
import com.dating.recepieapp.ui.activities.RecipePage
import com.dating.recepieapp.viewModel.RecipeViewModel
import com.dating.recepieapp.viewModel.RecipeViewmodelFactory
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Home : Fragment() {

    private var _binding:FragmentHome2Binding? = null
    private val binding get() = _binding!!

    private lateinit var auth:FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var homeCategoryAdapter: HomeCategoryAdapter
    private lateinit var homeRecipeAdapter: HomeRecipeAdapter
    private var dialog:AlertDialogBox? = null
    private lateinit var viewModel: RecipeViewModel
    private var networkConnection: NetworkConnection? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home2, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        dialog = AlertDialogBox()
        networkConnection = NetworkConnection()

        val factory = RecipeViewmodelFactory(networkConnection!!,dialog!!,requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[RecipeViewModel::class.java]

        setupUserData()

        binding.searchView.setOnClickListener {
            findNavController().navigate(R.id.action_home2_to_searchFragment)
        }

        //set user image
        viewModel.userData.observe(viewLifecycleOwner){
            Glide.with(requireView()).load(it?.imageUri).error(R.drawable.profile_default_image).into(binding.ivUserImage)
        }

        val query: Query = firestore.collection("PublicRecipe")

        val options: FirestoreRecyclerOptions<RecipeModel> = FirestoreRecyclerOptions.Builder<RecipeModel>()
            .setQuery(query, RecipeModel::class.java).build()
        homeRecipeAdapter = HomeRecipeAdapter(options)

        setUpRecipeRecyclerView()

        homeRecipeAdapter.setOnItemClickListener(object :HomeRecipeAdapter.OnItemClickListener{
            override fun onItemClick(recipeModel: RecipeModel) {
                val recipeIntent = Intent(requireActivity(),RecipePage::class.java)
                recipeIntent.putExtra("recipe_name",recipeModel.title)
                recipeIntent.putExtra("recipe_image_url",recipeModel.imageUrl)
                recipeIntent.putExtra("recipe_recipe",recipeModel.recipe)
                recipeIntent.putExtra("recipe_mode",recipeModel.mode)
                recipeIntent.putExtra("recipe_id",recipeModel.postId)
                recipeIntent.putExtra("recipe_cat_id",recipeModel.categoryId)
                startActivity(recipeIntent)
            }
        })

        return binding.root

    }

    private fun setupUserData(){
        val image = auth.currentUser?.photoUrl.toString()
        val userName = "Kapil Saini"
        val errorImage = ResourcesCompat.getDrawable(resources,R.drawable.profile_default_image,null)
        Glide.with(requireContext()).load(image).error(errorImage).into(binding.ivUserImage)
        binding.tvUserName.text = userName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog = null
        Glide.with(requireView()).clear(binding.ivUserImage)

        _binding = null
    }

//    private fun setupRecyclerView(){
//
//        val query: Query = firestore.collection("PublicCategory")
//
//        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
//
//        val options: FirestoreRecyclerOptions<CategoriesModel> = FirestoreRecyclerOptions.Builder<CategoriesModel>()
//            .setQuery(query, CategoriesModel::class.java).build()
//        homeCategoryAdapter = HomeCategoryAdapter(options)
//
//        binding.rvCategory.apply {
//            this.adapter = homeCategoryAdapter
//            this.layoutManager = layoutManager
//        }
//        homeCategoryAdapter.startListening()
//    }

    private fun setUpRecipeRecyclerView(){

        val layoutManager = GridLayoutManager(requireContext(),2)

        binding.rvRecipe.apply {
            this.adapter = homeRecipeAdapter
            this.layoutManager = layoutManager
        }
        homeRecipeAdapter.startListening()
    }



}