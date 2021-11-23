package com.dzenis_ska.testmessenger.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import com.dzenis_ska.testmessenger.db.Dialog


class DiffUtilDialogs(val oldList: List<Dialog>, val newList: List<Dialog>): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].time == newList[newItemPosition].time
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}