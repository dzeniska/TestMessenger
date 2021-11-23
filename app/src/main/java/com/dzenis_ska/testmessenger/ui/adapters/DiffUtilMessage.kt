package com.dzenis_ska.testmessenger.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import com.dzenis_ska.testmessenger.db.Dialog
import com.dzenis_ska.testmessenger.db.Messages

class DiffUtilMessage(val oldList: List<Messages>, val newList: List<Messages>): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}