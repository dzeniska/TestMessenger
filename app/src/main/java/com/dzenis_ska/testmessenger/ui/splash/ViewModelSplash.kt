package com.dzenis_ska.testmessenger.ui.splash

import android.util.Log
import androidx.lifecycle.*
import com.dzenis_ska.testmessenger.db.FBAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class ViewModelSplash(val auth: FBAuth) : ViewModel() {

    private val _launchCurrentUser = MutableLiveData<String>()
    val launchCurrentUser: LiveData<String?> = _launchCurrentUser

    init {
        viewModelScope.launch {
            delay(1500)
            Log.d("!!!init", "splash")
            auth.isCurUser {
                _launchCurrentUser.value = it?.displayName
            }
        }
    }

    class SplashViewModelFactory(private val auth: FBAuth) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(ViewModelSplash::class.java)){
                @Suppress("UNCHECKED_CAST")
                return ViewModelSplash(auth) as T
            }
            throw IllegalArgumentException("Unknown ViewModel Class")
        }
    }
}