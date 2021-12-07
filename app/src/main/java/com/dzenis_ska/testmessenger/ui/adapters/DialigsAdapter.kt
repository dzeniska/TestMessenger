package com.dzenis_ska.testmessenger.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.testmessenger.databinding.ItemDialogsBinding
import com.dzenis_ska.testmessenger.db.Dialog
import java.text.SimpleDateFormat
import java.util.*

interface ChooseUserInterface {
    fun chooseUser(user: Dialog)

}

class DialogsAdapter(private val chooseUserInterface: ChooseUserInterface ) : RecyclerView.Adapter<DialogsAdapter.DialogsViewHolder>(), View.OnClickListener {

    var dialogs: List<Dialog> = emptyList()
        set(newValue) {

            val diffResult = DiffUtil.calculateDiff(DiffUtilDialogs(field, newValue))
            field = newValue
            diffResult.dispatchUpdatesTo(this)


//            notifyDataSetChanged()
        }

    override fun onClick(v: View) {
        val user = v.tag as Dialog
        chooseUserInterface.chooseUser(user)
    }

    override fun getItemCount(): Int = dialogs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogsViewHolder {
        Log.d("!!!rcView", "${viewType} _ ${parent}")
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDialogsBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this)

        return DialogsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DialogsViewHolder, position: Int) {
        val dialog = dialogs[position]

        with(holder.binding) {

            root.tag = dialog

            tvUName.text = dialog.name

            tvUEmail.text = dialog.email

            val countUnreadMess = dialog.countUnreadMess.toString()
            if(countUnreadMess.toInt() < 1){
                tvCountUnreadMess.isVisible = false
            } else if(countUnreadMess.toInt() < 10){
                tvCountUnreadMess.isVisible = true
                tvCountUnreadMess.text = dialog.countUnreadMess.toString()
            } else {
                tvCountUnreadMess.isVisible = true
                tvCountUnreadMess.text = "+9"
            }

            val sdf = SimpleDateFormat("HH:mm")
            val resultDate = Date(dialog.time.toLong())
            tvTimeLastMessage.text = sdf.format(resultDate)
        }
    }

    class DialogsViewHolder(val binding: ItemDialogsBinding): RecyclerView.ViewHolder(binding.root)
}