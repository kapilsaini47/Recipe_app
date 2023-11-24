package com.dating.recepieapp.ui.fragment

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
import com.dating.recepieapp.adapter.CategoriesAdapter
import com.dating.recepieapp.databinding.FragmentRecipesBinding
import com.dating.recepieapp.helper.AlertDialogBox
import com.dating.recepieapp.helper.NetworkConnection
import com.dating.recepieapp.models.CategoriesModel
import com.dating.recepieapp.ui.activities.AddNewCategory
import com.dating.recepieapp.ui.activities.AddNewRecipe
import com.dating.recepieapp.viewModel.RecipeViewModel
import com.dating.recepieapp.viewModel.RecipeViewmodelFactory
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Recipes : Fragment() {

    private var _binding:FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter : CategoriesAdapter
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var auth:FirebaseAuth
    private lateinit var uid:String
    private lateinit var viewModel:RecipeViewModel
    private var dialog: AlertDialogBox? = null
    private var networkConnection: NetworkConnection? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_recipes, container, false)

        dialog = AlertDialogBox()
        networkConnection = NetworkConnection()
        firebaseFirestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()


        val factory = RecipeViewmodelFactory(networkConnection!!,dialog!!, requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[RecipeViewModel::class.java]

        binding.fabAddCategory.setOnClickListener {
            val intent = Intent(requireActivity(), AddNewCategory::class.java)
            startActivity(intent)
        }

        //notify user about user didn't create any recipe yet
        viewModel.userCategoryData.observe(viewLifecycleOwner){
            if (it.isNullOrEmpty()){
                binding.ivEmpty.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.VISIBLE
            }else{
                binding.ivEmpty.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
            }
        }

        val query: Query = firebaseFirestore.collection("User")
            .document(uid).collection("Category")
        val options: FirestoreRecyclerOptions<CategoriesModel> = FirestoreRecyclerOptions.Builder<CategoriesModel>()
            .setQuery(query,CategoriesModel::class.java).build()

        categoryAdapter = CategoriesAdapter(options)
        categoryAdapter.setOnItemClickListener(object : CategoriesAdapter.OnItemClickListener{
            override fun onItemClick(categoryModel: CategoriesModel) {
                val intent = Intent(requireActivity(),AddNewRecipe::class.java)
                intent.putExtra("categoryId",categoryModel.recipeCategoryId)
                intent.putExtra("categoryName",categoryModel.name)
                startActivity(intent)
            }
        })


        setupRecyclerView()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        categoryAdapter.stopListening()

        _binding = null
    }

    private fun setupRecyclerView(){

        val layoutManager = GridLayoutManager(context, 2)

        binding.rvRecipeCategory.apply {
            this.adapter = categoryAdapter
            this.layoutManager = layoutManager
        }
        categoryAdapter.startListening()
    }

}