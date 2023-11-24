package com.dating.recepieapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.layout.Layout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dating.recepieapp.R
import com.dating.recepieapp.models.CategoriesModel
import com.dating.recepieapp.models.RecipeModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class HomeRecipeAdapter(
    options: FirestoreRecyclerOptions<RecipeModel>
):FirestoreRecyclerAdapter<RecipeModel,HomeRecipeAdapter.RecipeViewHolder>(options) {

    interface OnItemClickListener {
        fun onItemClick(recipeModel: RecipeModel)
    }

    private var onItemClickListener: OnItemClickListener? = null

    // Public method to set the OnItemClickListener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }


    inner class RecipeViewHolder(view:View):RecyclerView.ViewHolder(view) {
        val image = view.findViewById<ImageView>(R.id.ivRecipeImageSingle)
        val recipeName = view.findViewById<TextView>(R.id.tvRecipeNameSingle)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        return RecipeViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.single_recipe_layout,parent,false
        ))
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int, model: RecipeModel) {
        holder.recipeName.text = model.title
        Glide.with(holder.image.context).load(model.imageUrl).error(R.drawable.recipe_image).into(holder.image)
        // Set the click listener for the entire item view
        holder.itemView.setOnClickListener {
            val currentPosition = holder.absoluteAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onItemClickListener?.onItemClick(getItem(position))
            }
        }
    }
}