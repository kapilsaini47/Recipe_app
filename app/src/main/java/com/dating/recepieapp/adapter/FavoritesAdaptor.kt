package com.dating.recepieapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dating.recepieapp.R
import com.dating.recepieapp.models.FavoriteRecipeModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class FavoritesAdaptor(
options: FirestoreRecyclerOptions<FavoriteRecipeModel>
): FirestoreRecyclerAdapter<FavoriteRecipeModel, FavoritesAdaptor.FavoriteViewHolder>(options) {

    //Item click listener
    interface OnItemClickListener {
        fun onItemClick(favoriteRecipeModel: FavoriteRecipeModel)
    }

    //favorite button click listener
    interface OnFavoriteClickListener{
        fun onItemClick(position: Int,favoriteRecipeModel: FavoriteRecipeModel)
    }

    //Share button click listener
    interface OnShareClickListener{
        fun onItemClick(position: Int,favoriteRecipeModel: FavoriteRecipeModel)
    }

    private var onItemClickListener: OnItemClickListener? = null
    private var likeButtonClickListener: OnFavoriteClickListener? = null
    private var shareButtonClickListener: OnShareClickListener? = null

    // Public method to set the OnItemClickListener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun setOnFavoriteClickListener(listener: OnFavoriteClickListener){
        likeButtonClickListener = listener
    }

    fun setOnShareClickListener(listener: OnShareClickListener){
        shareButtonClickListener = listener
    }

    inner class FavoriteViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val recipeName: TextView = view.findViewById(R.id.tvRecipeNameFavorite)
        val recipeImage: ImageView = view.findViewById(R.id.ivRecipeImageFavorite)
        val recipeFavorites: ImageView = view.findViewById(R.id.ivFavorites)
        val recipeShare: ImageView = view.findViewById(R.id.ivShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        return FavoriteViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.single_recipe_favorite_layout,parent,false
            ))
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int, model: FavoriteRecipeModel) {
        holder.recipeName.text = model.title
        Glide.with(holder.itemView.context).load(model.imageUrl).error(R.drawable.recipe_image).into(holder.recipeImage)
        // Set the click listener for the entire item view
        holder.itemView.setOnClickListener {
            val currentPosition = holder.absoluteAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onItemClickListener?.onItemClick(getItem(position))
            }
        }

        holder.recipeFavorites.setOnClickListener {
            likeButtonClickListener?.onItemClick(position,getItem(position))
        }

        holder.recipeShare.setOnClickListener {
            shareButtonClickListener?.onItemClick(position,getItem(position))
        }
    }
}