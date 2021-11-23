package com.dzenis_ska.testmessenger.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.dzenis_ska.testmessenger.R
import com.dzenis_ska.testmessenger.databinding.ItemAdapterApponentMessageBinding
import com.dzenis_ska.testmessenger.databinding.ItemAdapterMyMessageBinding
import com.dzenis_ska.testmessenger.databinding.ItemAdapterTimeBinding
import com.dzenis_ska.testmessenger.db.Dialog
import com.dzenis_ska.testmessenger.db.Messages
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class MessageAdapter() : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    var messages: MutableList<Messages> = mutableListOf()
        set(newValue) {

            val diffResult = DiffUtil.calculateDiff(DiffUtilMessage(messages, newValue))
            diffResult.dispatchUpdatesTo(this)

            field = newValue
            notifyDataSetChanged()
        }



    override fun getItemCount(): Int = messages.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = when(viewType){
            MY_MESSAGE -> ItemAdapterMyMessageBinding.inflate(inflater, parent, false)
            TIME_SPACE -> ItemAdapterTimeBinding.inflate(inflater, parent, false)
            HIS_MESSAGE -> ItemAdapterApponentMessageBinding.inflate(inflater, parent, false)
            else -> throw IllegalArgumentException("Invalid view type")
        }
        return MessageViewHolder(binding)
    }
fun erf(){

}
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {

        val message = messages[position]
        with(holder) {
            when (message){
                is Messages.MyMessage -> {
                    binding as ItemAdapterMyMessageBinding
                    if(message.message?.isNotEmpty() == true){
                        binding.tvMessage.isVisible = true
                        binding.tvMessage.text = message.message
                    } else {
                        binding.tvMessage.isVisible = false
                    }
                    val sdf = SimpleDateFormat("HH:mm")
                    val resultDate = Date(message.time.toLong())
                    binding.tvTime.text = sdf.format(resultDate)
                    if (message.isRead) {
                        binding.ivDone.setImageResource(R.drawable.ic_done_all)
                    } else {
                        binding.ivDone.setImageResource(R.drawable.ic_done)
                    }
                    Log.d("!!!loadImage", "${message.photoUrl}")
                    Log.d("!!!loadImage", "${message.email}")

                    if(message.photoUrl != "null"){
                        binding.progressBar.isVisible = true
                        binding.ivImageMess.isVisible = true

                        Log.d("!!!loadImage", "${message.photoUrl}")

                        Picasso.get()
                            .load(message.photoUrl)
                            .placeholder(R.drawable.ic_image_300_300)
                            .error(R.drawable.ic_no_connection)
                            .into(binding.ivImageMess, object : Callback{
                                override fun onSuccess() {
                                    binding.progressBar.isVisible = false
                                }
                                override fun onError(e: Exception?) {
                                    binding.progressBar.isVisible = false
                                }
                            })
                    } else {
                        binding.progressBar.isVisible = false
                        binding.ivImageMess.visibility = View.GONE
                    }


                }
                is Messages.TimeSpace -> {
                    binding as ItemAdapterTimeBinding
                    val sdf = SimpleDateFormat("dd MM yyyy")
                    val resultDate = Date(message.time.toLong())
                    binding.tvTime.text = sdf.format(resultDate)
                }
                is Messages.HisMessage -> {

                    binding as ItemAdapterApponentMessageBinding
                    if(message.message?.isNotEmpty() == true){
                        binding.tvMessage.isVisible = true
                        binding.tvMessage.text = message.message
                    } else {
                        binding.tvMessage.isVisible = false
                    }
                    val sdf = SimpleDateFormat("HH:mm")
                    val resultDate = Date(message.time.toLong())
                    binding.tvTime.text = sdf.format(resultDate)

                    if(message.photoUrl != "null"){
                        binding.progressBar.isVisible = true
                        binding.ivImageMess.isVisible = true

                        Log.d("!!!loadImageapp", "${message.photoUrl} _ ${message.email}")

                        Picasso.get()
                            .load(message.photoUrl)
                            .placeholder(R.drawable.ic_image_300_300)
                            .error(R.drawable.ic_no_connection)
                            .into(binding.ivImageMess, object : Callback{
                                override fun onSuccess() {
                                    binding.progressBar.isVisible = false
                                }
                                override fun onError(e: Exception?) {
                                    binding.progressBar.isVisible = false
                                }
                            })
                    } else {
                        binding.progressBar.isVisible = false
                        binding.ivImageMess.visibility = View.GONE
                    }
                }
            }
        }
    }

    class MessageViewHolder(val binding: ViewBinding): RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return when(messages[position]){
            is Messages.MyMessage -> MY_MESSAGE
            is Messages.TimeSpace -> TIME_SPACE
            else -> HIS_MESSAGE
        }
    }

    companion object {
        private const val MY_MESSAGE = 0
        private const val TIME_SPACE = 1
        private const val HIS_MESSAGE = 2

    }
}