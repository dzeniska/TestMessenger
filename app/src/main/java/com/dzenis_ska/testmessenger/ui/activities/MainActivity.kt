package com.dzenis_ska.testmessenger.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.dzenis_ska.testmessenger.R
import com.dzenis_ska.testmessenger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var navController: NavController? = null

    var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)



        val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHost.navController
        val graph = navController!!.navInflater.inflate(R.navigation.main_graph)
        if(isSignedIn() == null){
            graph.setStartDestination(R.id.signInFragment)
        }else{
            graph.setStartDestination(R.id.homeFragment)
        }
        navController!!.graph = graph
    }

    override fun onBackPressed() {
            super.onBackPressed()
    }

    private fun isSignedIn(): String? {
        val bundle = intent.extras ?: throw IllegalStateException("No required arguments")
        val args = MainActivityArgs.fromBundle(bundle)
        return args.name
    }
}