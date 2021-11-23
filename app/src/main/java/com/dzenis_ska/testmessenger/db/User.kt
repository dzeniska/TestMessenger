package com.dzenis_ska.testmessenger.db

import android.os.Parcelable
import com.google.firebase.firestore.FieldValue
import kotlinx.parcelize.Parcelize

@Parcelize
class User(
    val name: String,
    val email: String,
    val photoUrl: String,
    val emailVerified: Boolean,
    val uid: String,

): Parcelable
