package com.dzenis_ska.testmessenger.db

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


sealed class Messages
//    (
//    val times: String
//)
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
    ): Messages(), Parcelable

    @Parcelize
    data class TimeSpace(
        val time: String
    ): Messages(), Parcelable

    @Parcelize
    data class HisMessage(
        val name: String,
        val email: String,
        val message: String?,
        @field:JvmField
        val isRead: Boolean = false,
        val time: String,
        val photoUrl: String? = null
    ): Messages(), Parcelable
}
