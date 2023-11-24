package com.dating.recepieapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dating.recepieapp.R
import com.dating.recepieapp.models.RequestModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class ReceiveRequestAdapter(
    options: FirestoreRecyclerOptions<RequestModel>
): FirestoreRecyclerAdapter<RequestModel, ReceiveRequestAdapter.RequestViewHolder>(options) {

    interface OnItemClickListener {
        fun rejectResponse(position: Int,requestModel: RequestModel)
    }

    interface OnItemApproveClickListener {
        fun approveResponse(position: Int,requestModel: RequestModel)
    }

    private var onApproveClickListener: OnItemApproveClickListener? = null
    private var onRejectClickListener: OnItemClickListener? = null

    // Public method to set the OnItemClickListener
    fun setOnApproveClickListener(listener: OnItemApproveClickListener) {
        onApproveClickListener = listener
    }
    fun setOnRejectClickListener(listener: OnItemClickListener) {
        onRejectClickListener = listener
    }

    inner class RequestViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val receiveRecipeName = view.findViewById<TextView>(R.id.tvReceivedRequestRecipeName)
        val btnApprove = view.findViewById<ImageView>(R.id.ivAcceptRequest)
        val btnReject = view.findViewById<ImageView>(R.id.ivRejectRequest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        return RequestViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.single_item_recieved_request_layout,parent,false
            ))
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int, model: RequestModel) {
        holder.receiveRecipeName.text = model.recipeName
        // Set the click listener for the entire item view

        holder.btnApprove.setOnClickListener {
            val currentPosition = holder.absoluteAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onApproveClickListener?.approveResponse(position,getItem(position))
            }
        }

        holder.btnReject.setOnClickListener {
            val currentPosition = holder.absoluteAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onRejectClickListener?.rejectResponse(position,getItem(position))
            }
        }
    }

}