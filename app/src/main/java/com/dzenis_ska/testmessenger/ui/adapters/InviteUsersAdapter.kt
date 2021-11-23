package com.dzenis_ska.testmessenger.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.testmessenger.R
import com.dzenis_ska.testmessenger.databinding.ItemInviteBinding
import com.dzenis_ska.testmessenger.db.User
import com.dzenis_ska.testmessenger.ui.fragments.InvitationsFragment

interface InvUserInterface {
    fun onUserDecline(user: User)
    fun onUserAccept(user: User)
}

class InviteUsersAdapter(private val invUserInterface: InvUserInterface ) : RecyclerView.Adapter<InviteUsersAdapter.UsersViewHolder>(), View.OnClickListener {

    var users: List<User> = emptyList()
        set(newValue) {

            val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(users, newValue))
            diffResult.dispatchUpdatesTo(this)

            field = newValue
            notifyDataSetChanged()
        }

    override fun onClick(v: View) {
        val user = v.tag as User
        when (v.id) {
            R.id.bDecline -> {
                invUserInterface.onUserDecline(user)
            }
            R.id.bAccept -> {
                invUserInterface.onUserAccept(user)
            }
            else -> Log.d("!!!", "sdfsdf")
        }
    }

    override fun getItemCount(): Int = users.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemInviteBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this)
        binding.bDecline.setOnClickListener(this)
        binding.bAccept.setOnClickListener(this)

        return UsersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user = users[position]
        with(holder.binding) {
            root.tag = user
            bDecline.tag = user
            bAccept.tag = user

            tvName.text = user.name
            tvEmail.text = user.email
        }
    }



    class UsersViewHolder(val binding: ItemInviteBinding): RecyclerView.ViewHolder(binding.root)
}