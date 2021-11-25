package com.dzenis_ska.testmessenger.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
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

        initAdapter()
        vm.listInviting()

        vm.invUsers.observe(viewLifecycleOwner, {
            adapter?.users = it as List<User>
        })
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