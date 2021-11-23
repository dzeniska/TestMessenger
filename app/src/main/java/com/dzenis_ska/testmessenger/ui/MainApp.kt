package com.dzenis_ska.testmessenger.ui

import android.app.Application
import com.dzenis_ska.testmessenger.db.FBAuth
import com.dzenis_ska.testmessenger.db.FBFirestore

class MainApp : Application() {

    val fbAuth by lazy {
        FBAuth(this)
    }

    val fbFirestore by lazy {
        FBFirestore(this)
    }
}