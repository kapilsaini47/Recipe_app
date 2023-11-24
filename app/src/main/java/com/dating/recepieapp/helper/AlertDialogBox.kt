package com.dating.recepieapp.helper

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.dating.recepieapp.R

class AlertDialogBox {

    fun showProgress(context: Context,activity: Activity) {
        val inflater:LayoutInflater = activity.layoutInflater
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setView(inflater.inflate(R.layout.progress_bar_layout,null))

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun noInternetAlert(context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("No Internet")
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setIcon(R.drawable.no_internet_icon)
        alertDialogBuilder.setPositiveButton("Retry") { _, _ ->
            // Perform the retry action here
            // You can add your retry logic here
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            // Close the app or take appropriate action on cancel
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}