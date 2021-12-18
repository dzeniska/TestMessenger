package com.dzenis_ska.testmessenger.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import com.dzenis_ska.testmessenger.db.Dialog
import com.dzenis_ska.testmessenger.db.Messages

class DiffUtilMessage(val oldList: List<Messages>, val newList: List<Messages>): DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size


    override fun getNewListSize(): Int = newList.size


    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMess = oldList[oldItemPosition]
        val newMess = newList[newItemPosition]
        return oldMess.times == newMess.times
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMess = oldList[oldItemPosition]
        val newMess = newList[newItemPosition]
        return oldMess.readed == newMess.readed
    }

}