package com.dzenis_ska.testmessenger.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzenis_ska.testmessenger.R
import com.dzenis_ska.testmessenger.databinding.FragmentInvitationsBinding
import com.dzenis_ska.testmessenger.db.User
import com.dzenis_ska.testmessenger.ui.MainApp
import com.dzenis_ska.testmessenger.ui.activities.ViewModelMain
import com.dzenis_ska.testmessenger.ui.adapters.InvUserInterface
import com.dzenis_ska.testmessenger.ui.adapters.InviteUsersAdapter

class InvitationsFragment : Fragment(R.layout.fragment_invitations) {

    private var binding: FragmentInvitationsBinding? = null
    private var adapter: InviteUsersAdapter? = null
    private val vm: ViewModelMain by activityViewModels {
        ViewModelMain.MainViewModelFactory((context?.applicationContext as MainApp).fbAuth, (context?.applicationContext as MainApp).fbFirestore)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInvitationsBinding.bind(view)

        initSpinner()
        initAdapter()
        vm.listInviting()

        initViewModel()

    }

    private fun initViewModel() = with(binding!!){
        vm.invUsers.observe(viewLifecycleOwner, {
            progressBar.isVisible = false
            if(it.size < 1) tvInvitations.text = getString(R.string.noDialogs)
            adapter?.users = it as List<User>
        })
    }

    private fun initSpinner(){
        val listInv = arrayListOf<String>()
        listInv.add("InviteList: ")
        vm.getListInvite(){
            it?.forEach { email->
                listInv.add(email.toString())
            }
            if(listInv.size > 1){
                initSpinnerAdapter(listInv)
            }

        }
    }

    private fun initSpinnerAdapter(listInv: ArrayList<String>) = with(binding!!){
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listInv)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
//        spinner.prompt = "InviteList"
    }




    private fun initAdapter() = with(binding!!){
        adapter = InviteUsersAdapter(object : InvUserInterface{
            override fun onUserDecline(user: User) {
                vm.declineUser(user)
            }
            override fun onUserAccept(user: User) {
                vm.createDialog(user){
                    vm.declineUser(user)
                    Log.d("!!!isCreate", "$it")
                }
            }
        })
        val layoutManager = LinearLayoutManager(context)
        rcViewInvite.layoutManager = layoutManager
        rcViewInvite.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vm.clearInvUsersList()
        binding = null
    }

}