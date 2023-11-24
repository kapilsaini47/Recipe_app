package com.dating.recepieapp.viewModel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dating.recepieapp.helper.AlertDialogBox
import com.dating.recepieapp.helper.NetworkConnection
import com.dating.recepieapp.models.CategoriesModel
import com.dating.recepieapp.models.FavoriteRecipeModel
import com.dating.recepieapp.models.RequestModel
import com.dating.recepieapp.models.UserModel
import com.dating.recepieapp.repository.Repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class RecipeViewModel (
    private val networkConnection: NetworkConnection,
    private val alertDialogBox: AlertDialogBox,
    val app: Application
    ):ViewModel() {

    private val _firebaseSavedQuote: MutableLiveData<List<FavoriteRecipeModel>> = MutableLiveData()
    val firebaseQuoteData : LiveData<List<FavoriteRecipeModel>> get() = _firebaseSavedQuote

    private val _likeStatus: MutableLiveData<List<FavoriteRecipeModel>> = MutableLiveData()
    val likeStatus: LiveData<List<FavoriteRecipeModel>>
        get() = _likeStatus

    private val _userData:MutableLiveData<UserModel?> = MutableLiveData()
    val userData : MutableLiveData<UserModel?> get() = _userData

    private val _userCategoryData:MutableLiveData<List<CategoriesModel>> = MutableLiveData()
    val userCategoryData:LiveData<List<CategoriesModel>> get() = _userCategoryData

    val db = FirebaseFirestore.getInstance()
    private val userId = Firebase.auth.currentUser?.uid

    init {
        getSavedFromFirebase()
        getUserData()
        getUserCategoryDataExistOrNot()
    }

    private fun getUserData(){
         if (networkConnection.hasInternetConnection(app)){
             db.collection("User").document(userId.toString()).get()
                 .addOnCompleteListener {response->
                     if (response.isSuccessful){
                         val userData = response.result.toObject(UserModel::class.java)

                         if (userData != null){
                             _userData.value = userData
                         }else{
                             _userData.value = null
                         }
                     }

             }

        }
    }

    private fun getSavedFromFirebase(){
        if (networkConnection.hasInternetConnection(app)){
            if (userId != null) {
                db.collection("User").document(userId).collection("Favorites")
                    .get().addOnCompleteListener {response->
                    if (response.isSuccessful){
                        val quoteList = mutableListOf<FavoriteRecipeModel>()
                        for (document in response.result){
                            val quote = document.toObject(FavoriteRecipeModel::class.java)
                            quoteList.add(quote)
                        }
                        _firebaseSavedQuote.postValue(quoteList)
                    }else{
                        Toast.makeText(app,response.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }else{
            alertDialogBox.noInternetAlert(app)
        }
    }

    fun deleteQuoteFromFirebase(documentId: String){
        if (networkConnection.hasInternetConnection(app)){
            if (userId != null){
                db.collection("User").document(userId).collection("Favorites")
                    .document(documentId).delete()
                    .addOnCompleteListener {
                        getSavedFromFirebase()
                    }
                    .addOnFailureListener {exception->
                        Toast.makeText(app,exception.message.toString(), Toast.LENGTH_LONG).show()
                    }
            }
        }else{
            alertDialogBox.noInternetAlert(app)
        }
    }

    fun addQuoteToFirebase(favoriteRecipeModel: FavoriteRecipeModel){
        if (networkConnection.hasInternetConnection(app)){
            val data = FavoriteRecipeModel(
                favoriteRecipeModel.favoriteId,favoriteRecipeModel.postId,favoriteRecipeModel.title,
                favoriteRecipeModel.imageUrl, favoriteRecipeModel.recipe, favoriteRecipeModel.ratings)
            if (userId != null){
                db.collection("User").document(userId).collection("Favorites")
                    .document(favoriteRecipeModel.favoriteId).set(data)
                    .addOnCompleteListener {
                    getSavedFromFirebase()
                }
                    .addOnFailureListener {exception->
                        Toast.makeText(app,exception.message.toString(), Toast.LENGTH_SHORT).show()
                    }
            }
        }else{
            alertDialogBox.noInternetAlert(app)
        }
    }

    fun toggleLikeButton(){
        val favoriteRecipes = mutableListOf<FavoriteRecipeModel>()
        if (networkConnection.hasInternetConnection(app)){
            if (userId != null) {
                db.collection("User").document(userId).collection("favorite")
                    .get().addOnCompleteListener {response->
                    if (response.isSuccessful){
                        favoriteRecipes.clear()
                        for (document in response.result){
                            val favoriteRecipe = document.toObject(FavoriteRecipeModel::class.java)
                            favoriteRecipe.let {
                                favoriteRecipes.add(it)
                            }
                        }
                        _likeStatus.postValue(favoriteRecipes)
                    }else{
                        Toast.makeText(app,response.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }else{
            alertDialogBox.noInternetAlert(app)
        }
    }

    fun sendRequestToUser(requestModel: RequestModel): CollectionReference? {
        var ref:CollectionReference? = null
        if (networkConnection.hasInternetConnection(app)){
            ref = db.collection("User").document(requestModel.userId).collection("ReceiveRequest")
        }
        return ref
    }

    fun saveSentRequest(requestModel: RequestModel): CollectionReference? {
        var ref:CollectionReference? = null
        if (networkConnection.hasInternetConnection(app)){
            ref = db.collection("User").document(requestModel.userId).collection("SentRequest")
        }
        return ref
    }

    fun addNewCategory(): CollectionReference? {
        var ref:CollectionReference? = null
        if (networkConnection.hasInternetConnection(app)){
            ref =  db.collection("User").document(userId!!)
                .collection("Category")
        }
        return ref
    }

    fun addPublicCategory():CollectionReference?{
        var ref:CollectionReference? = null
        if (networkConnection.hasInternetConnection(app)){
            ref = db.collection("PublicCategory")
        }
        return ref
    }

    fun generateId(): String? {
        var id:String? =null
        if (networkConnection.hasInternetConnection(app)){
            id = db.collection("PublicCategory").document().id
        }
        return  id
    }


    private fun getUserCategoryDataExistOrNot(){
        val dataList = mutableListOf<CategoriesModel>()
        if (networkConnection.hasInternetConnection(app)){
            db.collection("User")
                .document(userId!!).collection("Category").get().addOnCompleteListener {
                    if(it.isSuccessful){
                        dataList.clear()
                        for (document in it.result){
                            val data = document.toObject(CategoriesModel::class.java)
                            dataList.add(data)
                        }
                        _userCategoryData.value = dataList
                    }
                }
        }

    }





}