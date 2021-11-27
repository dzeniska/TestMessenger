package com.dzenis_ska.testmessenger.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dzenis_ska.testmessenger.R
import com.dzenis_ska.testmessenger.databinding.FragmentInviteMemberBinding
import com.dzenis_ska.testmessenger.ui.MainApp
import com.dzenis_ska.testmessenger.ui.activities.ViewModelMain


class InviteMemberFragment : Fragment(R.layout.fragment_invite_member) {

    var emailAdapter: ArrayAdapter<String>? = null

    var listEmail = emptyList<String>()
    var selectItemListEmail = emptyList<String>()

    private var binding: FragmentInviteMemberBinding? = null

    private val vm: ViewModelMain by activityViewModels {
        ViewModelMain.MainViewModelFactory(
            (context?.applicationContext as MainApp).fbAuth,
            (context?.applicationContext as MainApp).fbFirestore
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInviteMemberBinding.bind(view)


        initEmailAdapter()
        init()

        initOnClick()

        val fList = findNavController().backQueue
        fList.forEach {
            Log.d("!!!frFFF", "${it.destination.label}")
        }
    }

    private fun init() = with(binding!!) {
        vm.getListUsers() {
            listEmail = it
        }

        etEmail.addTextChangedListener {
            val list = arrayListOf<String>()
            listEmail.forEach { item ->
                val emailStart = it.toString()
                if (item.startsWith(emailStart)) list.add(item)
            }
            selectItemListEmail = list
            emailAdapter?.clear()
            emailAdapter?.addAll(list)

        }

    }

    private fun initEmailAdapter() = with(binding!!) {
        emailAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item)
        emailAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        listView.adapter = emailAdapter
        listView.setOnItemClickListener { parent, view, position, id ->
            if(selectItemListEmail.isNotEmpty()) binding!!.etEmail.setText(selectItemListEmail[position])
        }
    }

    private fun initOnClick() = with(binding!!) {

        bInviteUser.setOnClickListener {
            val email = etEmail.text.toString()
            if (email.isNotEmpty()
                &&
                email.contains('@')
                &&
                email.contains('.')
                &&
                !email.contains(' ')
            ) {

                vm.inviteUser(/*"${vm.getCurUser()?.email}",*/ email) {
                    if (it) Toast.makeText(context, "Приглашение отправленно!", Toast.LENGTH_LONG)
                        .show()
                    else Toast.makeText(context, "Приглашение НЕ отправленно!", Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                Toast.makeText(context, "Неверный формат email...", Toast.LENGTH_LONG)
                    .show()
            }
        }

    }
}