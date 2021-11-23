package com.dzenis_ska.testmessenger.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import com.dzenis_ska.testmessenger.db.User


class DiffUtilHelper(val oldList: List<User>, val newList: List<User>): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].uid == newList[newItemPosition].uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

//    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
//        if(oldItemPosition != newItemPosition) return oldItemPosition
//        return super.getChangePayload(oldItemPosition, newItemPosition)
//    }
//
//    val diffUtil = object : DiffUtil.ItemCallback<Ad>() {
//
//        override fun areItemsTheSame(oldItem: Ad, newItem: Ad) = oldItem.isFav == newItem.isFav
//
//        override fun areContentsTheSame(oldItem: Ad, newItem: Ad) = oldItem == newItem
//
//        override fun getChangePayload(oldItem: Ad, newItem: Ad): Any? {
//            if (oldItem.isFav != newItem.isFav) return newItem.isFav
//            return super.getChangePayload(oldItem, newItem)
//        }
//
//    }

}