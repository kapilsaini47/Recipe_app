package com.dating.recepieapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dating.recepieapp.R
import com.dating.recepieapp.models.CategoriesModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class HomeCategoryAdapter(
    options: FirestoreRecyclerOptions<CategoriesModel>
):FirestoreRecyclerAdapter<CategoriesModel,HomeCategoryAdapter.CategoriesViewHolder>(options) {

    inner class CategoriesViewHolder(view: View): RecyclerView.ViewHolder(view){
        val categoryName = view.findViewById<TextView>(R.id.tvCategoryNameHome)
    }

//    //DiffUtil for difference callback
//    private val diffUtil = object : DiffUtil.ItemCallback<CategoriesModel>(){
//        override fun areItemsTheSame(oldItem: CategoriesModel, newItem: CategoriesModel): Boolean {
//            return oldItem.recipeCategoryId == newItem.recipeCategoryId
//        }
//
//        override fun areContentsTheSame(oldItem: CategoriesModel, newItem: CategoriesModel): Boolean {
//            return oldItem == newItem
//        }
//    }
//
//    val differ = AsyncListDiffer(this,diffUtil)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        return CategoriesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.single_home_category_layout, parent,false)
        )
    }

    override fun onBindViewHolder(
        holder: CategoriesViewHolder,
        position: Int,
        model: CategoriesModel
    ) {
        holder.categoryName.text = model.name
    }
}