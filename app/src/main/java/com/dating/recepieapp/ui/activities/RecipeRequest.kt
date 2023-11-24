package com.dating.recepieapp.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dating.recepieapp.R
import com.dating.recepieapp.adapter.ReceiveRequestAdapter
import com.dating.recepieapp.adapter.SentRequestAdapter
import com.dating.recepieapp.databinding.ActivityRecipeRequestBinding
import com.dating.recepieapp.helper.AlertDialogBox
import com.dating.recepieapp.helper.NetworkConnection
import com.dating.recepieapp.models.RequestModel
import com.dating.recepieapp.viewModel.RecipeViewModel
import com.dating.recepieapp.viewModel.RecipeViewmodelFactory
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RecipeRequest : AppCompatActivity() {

    private var _binding:ActivityRecipeRequestBinding? = null
    private val binding get() = _binding!!

    private var firebaseFirestore:FirebaseFirestore? = null
    private var uid:String? = null
    private lateinit var receiveRequestAdapter:ReceiveRequestAdapter
    private lateinit var sentRequestAdapter: SentRequestAdapter
    private lateinit var viewModel:RecipeViewModel
    private var dialog:AlertDialogBox? = null
    private var networkConnection:NetworkConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_RecepieApp)
        _binding = DataBindingUtil.setContentView(this,R.layout.activity_recipe_request)

        firebaseFirestore = FirebaseFirestore.getInstance()
        uid  = FirebaseAuth.getInstance().currentUser?.uid.toString()
        dialog = AlertDialogBox()
        networkConnection = NetworkConnection()

        val factory = RecipeViewmodelFactory(networkConnection!!,dialog!!, application)
        viewModel = ViewModelProvider(this, factory)[RecipeViewModel::class.java]

        setupReceiveRequestRecyclerView()
        setUpSentRequestRecyclerView()

        receiveRequestAdapter.setOnApproveClickListener(object: ReceiveRequestAdapter.OnItemApproveClickListener{
            override fun approveResponse(position: Int, requestModel: RequestModel) {
                val newRequestModel = RequestModel(
                    requestModel.requestId,requestModel.recipeName,requestModel.recipeId,requestModel.userId,true)

                firebaseFirestore!!.collection("User").document(requestModel.userId)
                    .collection("SentRequest").document(requestModel.requestId)
                    .set(newRequestModel).addOnCompleteListener {
                        if (it.isComplete){
                            deleteRequest(requestModel)
                            Toast.makeText(this@RecipeRequest,"Request Approved",Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener{
                        Toast.makeText(this@RecipeRequest,it.message.toString(),Toast.LENGTH_LONG).show()
                    }
            }
        })

        receiveRequestAdapter.setOnRejectClickListener(object :ReceiveRequestAdapter.OnItemClickListener{
            override fun rejectResponse(position: Int, requestModel: RequestModel) {
                val newRequestModel = RequestModel(
                    requestModel.requestId,requestModel.recipeName,requestModel.recipeId,requestModel.userId,false)

                firebaseFirestore!!.collection("User").document(requestModel.userId)
                    .collection("SentRequest").document(requestModel.requestId)
                    .set(newRequestModel).addOnCompleteListener {
                        if (it.isComplete){
                            Toast.makeText(this@RecipeRequest,"Request Rejected",Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener{
                        Toast.makeText(this@RecipeRequest,it.message.toString(),Toast.LENGTH_LONG).show()
                    }
            }

        })

        sentRequestAdapter.setOnItemClickListener(object :SentRequestAdapter.OnItemClickListener{
            override fun onItemClick(requestModel: RequestModel) {
                Toast.makeText(this@RecipeRequest,"Clicked",Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun setupReceiveRequestRecyclerView(){

        val query: Query = firebaseFirestore?.collection("User")?.document(uid!!)
            ?.collection("ReceiveRequest") as Query
        val options: FirestoreRecyclerOptions<RequestModel> = FirestoreRecyclerOptions.Builder<RequestModel>()
            .setQuery(query, RequestModel::class.java).build()

        receiveRequestAdapter = ReceiveRequestAdapter(options)
        val layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false)

        binding.rvRecieveRequest.apply {
            this.adapter = receiveRequestAdapter
            this.layoutManager = layoutManager
        }
        receiveRequestAdapter.startListening()
    }

    private fun setUpSentRequestRecyclerView(){
        val query: Query = firebaseFirestore?.collection("User")?.document(uid!!)
            ?.collection("SentRequest") as Query
        val options: FirestoreRecyclerOptions<RequestModel> = FirestoreRecyclerOptions.Builder<RequestModel>()
            .setQuery(query, RequestModel::class.java).build()

        sentRequestAdapter = SentRequestAdapter(options)
        val layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false)

        binding.rvSentRequest.apply {
            this.adapter = sentRequestAdapter
            this.layoutManager = layoutManager
        }
        sentRequestAdapter.startListening()
    }

    private fun deleteRequest(requestModel: RequestModel){
        firebaseFirestore!!.collection("User").document(uid!!)
            .collection("ReceiveRequest").document(requestModel.requestId)
            .delete()
    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseFirestore = null
        uid = null

        _binding = null
    }
}