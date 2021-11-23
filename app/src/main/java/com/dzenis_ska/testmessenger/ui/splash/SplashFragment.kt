package com.dzenis_ska.testmessenger.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.dzenis_ska.testmessenger.R
import com.dzenis_ska.testmessenger.databinding.FragmentSplashBinding
import com.dzenis_ska.testmessenger.ui.MainApp
import com.dzenis_ska.testmessenger.ui.activities.MainActivity
import com.dzenis_ska.testmessenger.ui.activities.MainActivityArgs

class SplashFragment: Fragment(R.layout.fragment_splash) {

    private var binding: FragmentSplashBinding? = null

    private val viewModelSplash: ViewModelSplash by activityViewModels {
        ViewModelSplash.SplashViewModelFactory((context?.applicationContext as MainApp).fbAuth)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSplashBinding.bind(view)
        initCurUser()
    }

    private fun initCurUser(){
        viewModelSplash.launchCurrentUser.observe(viewLifecycleOwner,{
//            Log.d("!!!", "${it}")
            if(it == null)
                initIntent(null)
            else
                initIntent(it)

        })
    }
    private fun initIntent(name: String?) {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val args = MainActivityArgs(name)
        intent.putExtras(args.toBundle())
        startActivity(intent)
    }
}