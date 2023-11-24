package com.dating.recepieapp.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dating.recepieapp.helper.AlertDialogBox
import com.dating.recepieapp.helper.NetworkConnection

class RecipeViewmodelFactory(
    private val networkConnection: NetworkConnection,
    private val alertDialogBox: AlertDialogBox,
    private val app: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            return RecipeViewModel( networkConnection, alertDialogBox,app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
