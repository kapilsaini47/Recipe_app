package com.dating.recepieapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.dating.recepieapp.R
import com.dating.recepieapp.interfaces.OnClickListener
import com.dating.recepieapp.models.CategoriesModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class CategoriesAdapter(
        options: FirestoreRecyclerOptions<CategoriesModel>
):FirestoreRecyclerAdapter<CategoriesModel,CategoriesAdapter.CategoriesViewHolder>(options) {

    interface OnItemClickListener {
        fun onItemClick(categoryModel: CategoriesModel)
    }

    private var onItemClickListener: OnItemClickListener? = null

    // Public method to set the OnItemClickListener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }


    inner class CategoriesViewHolder(view: View):RecyclerView.ViewHolder(view){
        val categoryName =
            view.findViewById<TextView>(R.id.tvCategoryName)
        val cardView = view.findViewById<CardView>(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        return CategoriesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.single_item_to_add_new_recipe_category, parent,false)
        )
    }

    override fun onBindViewHolder(
        holder: CategoriesViewHolder,
        position: Int,
        model: CategoriesModel
    ) {
        holder.categoryName.text = model.name
            // Set the click listener for the entire item view
        holder.itemView.setOnClickListener {
                val currentPosition = holder.absoluteAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(getItem(position))
                }
            }

    }



}