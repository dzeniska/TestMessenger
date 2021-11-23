package com.dzenis_ska.testmessenger.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions

import com.dzenis_ska.testmessenger.R
import com.dzenis_ska.testmessenger.databinding.FragmentHomeBinding
import com.dzenis_ska.testmessenger.ui.MainApp
import com.dzenis_ska.testmessenger.ui.activities.ViewModelMain
import com.google.android.material.tabs.TabLayout

class HomeFragment(): Fragment(R.layout.fragment_home) {


    private var binding: FragmentHomeBinding? = null
    private var navController: NavController? = null

    private val vm: ViewModelMain by activityViewModels {
        ViewModelMain.MainViewModelFactory((context?.applicationContext as MainApp).fbAuth, (context?.applicationContext as MainApp).fbFirestore)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        (activity as AppCompatActivity).setSupportActionBar(binding!!.toolbar)

        (activity as AppCompatActivity).supportActionBar?.title = ""

        val navHost = childFragmentManager.findFragmentById(R.id.tabContainer) as NavHostFragment
        navController = navHost.navController

        initTabLayout()
        setHasOptionsMenu(true)

    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.invite -> {
                val direction = HomeFragmentDirections.actionHomeFragmentToInviteMemberFragment("dzenbuddha@yandex.ru")
//                navController = null
                findNavController().navigate(direction)
                Log.d("!!!", "invite")
                true
            }
            R.id.logOut -> {
                val direction = HomeFragmentDirections.actionHomeFragmentToSignInFragment(true)
//                requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                findNavController().navigate(direction, navOptions {
                    popUpTo(R.id.homeFragment){
                        inclusive = true
                    }
                })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initTabLayout() = with(binding!!){

        Log.d("!!!tab", "${destinationFr()}")
        tabLayout.setScrollPosition(destinationFr(),0f,false);

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d("!!!tabClick", "onTabSelected")
                Log.d("!!!tab", "${tab?.text}")
                Log.d("!!!tab", "${destinationFr()}")
                if(tab?.text == "Invitations"){
//                    viewModelMain.directToInvFR(true)
                    if(destinationFr() == 0){
                        val direction = DialogsFragmentDirections.actionDialogsFragmentToInvitationsFragment(15)
                        Log.d("!!!", "$navController")
                        navController?.navigate(direction
                            ,
                            navOptions {
                                anim {
                                    enter = R.anim.enter
                                    exit = R.anim.exit
                                    popEnter = R.anim.pop_enter
                                    popExit = R.anim.pop_exit
                                }
                            }
                        )
                    }
                } else {
                    val fList = findNavController().backQueue
                    fList.forEach {
                        Log.d("!!!frFFFFNAVC", "${it.destination.label}")
                    }
                    navController?.backQueue
                    fList.forEach {
                        Log.d("!!!frFFFFNAVC", "${it.destination.label}")
                    }
                    navController?.popBackStack()
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
                if(tab?.text != "Invitations" && destinationFr() == 1){
                    navController?.popBackStack()
                }
                Log.d("!!!tabClick", "onTabReselected")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                Log.d("!!!tabClick", "onTabUnselected")
            }
        })
    }

    private fun destinationFr(): Int{
        return if(navController?.currentDestination?.label == "DialogsFragment") 0 else 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("!!!onDestroyView", "onDestroyView")
        binding = null
    }

}




