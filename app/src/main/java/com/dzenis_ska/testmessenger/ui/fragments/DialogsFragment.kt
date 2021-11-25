package com.dzenis_ska.testmessenger.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzenis_ska.testmessenger.R
import com.dzenis_ska.testmessenger.databinding.FragmentDialogsBinding
import com.dzenis_ska.testmessenger.db.Dialog
import com.dzenis_ska.testmessenger.ui.MainApp
import com.dzenis_ska.testmessenger.ui.activities.ViewModelMain
import com.dzenis_ska.testmessenger.ui.adapters.ChooseUserInterface
import com.dzenis_ska.testmessenger.ui.adapters.DialogsAdapter
import com.dzenis_ska.testmessenger.utils.findTopNavController


class DialogsFragment : Fragment(R.layout.fragment_dialogs) {

    private var binding: FragmentDialogsBinding? = null
    private var adapter: DialogsAdapter? = null
    private val viewModelMain: ViewModelMain by activityViewModels {
        ViewModelMain.MainViewModelFactory((context?.applicationContext as MainApp).fbAuth, (context?.applicationContext as MainApp).fbFirestore)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDialogsBinding.bind(view)


        initAdapter()

        initViewModel()
        viewModelMain.getDialogsList()
    }

    private fun initViewModel() {
        viewModelMain.listDialogs.observe(viewLifecycleOwner, {
            adapter?.dialogs = it
        })
    }

    private fun initAdapter() = with(binding!!) {
        adapter = DialogsAdapter( object : ChooseUserInterface{
            override fun chooseUser(message: Dialog) {
                val direction = HomeFragmentDirections.actionHomeFragmentToUserNameFragment(message)
                findTopNavController().navigate(direction)
                viewModelMain.decrementMess(message)
                Log.d("!!!click", "$message")
            }
        })
        val layoutManager = LinearLayoutManager(context)
        recyclerViewDialogs.layoutManager = layoutManager
        recyclerViewDialogs.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModelMain.clearDialogList()
        binding = null
    }

}