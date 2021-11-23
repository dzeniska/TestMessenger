package com.dzenis_ska.testmessenger.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.dzenis_ska.testmessenger.R
import com.dzenis_ska.testmessenger.databinding.FragmentEnterNameBinding
import com.dzenis_ska.testmessenger.ui.MainApp
import com.dzenis_ska.testmessenger.ui.activities.ViewModelMain
import com.dzenis_ska.testmessenger.utils.InitBackStack


class EnterNameFragment : Fragment(R.layout.fragment_enter_name) {

    private var binding: FragmentEnterNameBinding? = null

    private val viewModelMain: ViewModelMain by activityViewModels {
        ViewModelMain.MainViewModelFactory((context?.applicationContext as MainApp).fbAuth, (context?.applicationContext as MainApp).fbFirestore)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEnterNameBinding.bind(view)

        initClick()

    }



    private fun initClick() = with((binding!!)){
        bSignName.setOnClickListener {
            if(etName.text.isNotEmpty()){
                etName.isClickable = false
                viewModelMain.setName(etName.text.toString()) {name, isName ->
                    if(isName == true) {
                        findNavController().navigate(R.id.homeFragment)
                        Toast.makeText(context, "Hello, $name!", Toast.LENGTH_LONG).show()
                    }else{
                        etName.isClickable = true
                    }
                }
            }
        }
    }
}