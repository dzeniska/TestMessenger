package com.dzenis_ska.testmessenger.db

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Dialog(
    val name: String,
    val email: String,
    val time: String,
    val dialogName: String,
    val countUnreadMess: Int
): Parcelable
