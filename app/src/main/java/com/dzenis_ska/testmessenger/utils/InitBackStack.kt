package com.dzenis_ska.testmessenger.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.navigation.NavController


object InitBackStack {

    @SuppressLint("RestrictedApi")
    fun initBackStack(navController: NavController){
        val fList = navController.backQueue
        fList.forEach {
            Log.d("!!!frFF", "\n${it.destination.label}")
        }
    }
}