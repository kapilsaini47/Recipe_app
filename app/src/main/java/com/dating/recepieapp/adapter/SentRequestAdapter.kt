package com.dating.recepieapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dating.recepieapp.R
import com.dating.recepieapp.models.RecipeModel
import com.dating.recepieapp.models.RequestModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class SentRequestAdapter(
    options: FirestoreRecyclerOptions<RequestModel>
):FirestoreRecyclerAdapter<RequestModel,SentRequestAdapter.RequestViewHolder>(options) {

    interface OnItemClickListener {
        fun onItemClick(requestModel: RequestModel)
    }

    private var onItemClickListener: OnItemClickListener? = null

    // Public method to set the OnItemClickListener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    inner class RequestViewHolder(view:View):RecyclerView.ViewHolder(view) {
        val sentRecipeName = view.findViewById<TextView>(R.id.tvSendRequestRecipeName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        return RequestViewHolder(
            LayoutInflater.from(parent.context).inflate(
            R.layout.single_item_sent_request_layout,parent,false
        ))
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int, model: RequestModel) {
        holder.sentRecipeName.text = model.recipeName
        // Set the click listener for the entire item view
        holder.itemView.setOnClickListener {
            val currentPosition = holder.absoluteAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onItemClickListener?.onItemClick(getItem(position))
            }
        }
    }

}