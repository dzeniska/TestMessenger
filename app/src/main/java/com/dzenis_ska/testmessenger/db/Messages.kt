package com.dzenis_ska.testmessenger.db

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


sealed class Messages
    (
    val times: String,
    var readed: Boolean
)
{
    @Parcelize
    data class MyMessage(
        val name: String,
        val email: String,
        val message: String?,
        @field:JvmField
        val isRead: Boolean = false,
        val time: String,
        val photoUrl: String? = null
    ): Messages(time, isRead), Parcelable

    @Parcelize
    data class TimeSpace(
        val time: String
    ): Messages(time, false), Parcelable

    @Parcelize
    data class HisMessage(
        val name: String,
        val email: String,
        val message: String?,
        @field:JvmField
        val isRead: Boolean = false,
        val time: String,
        val photoUrl: String? = null
    ): Messages(time, isRead), Parcelable
}
