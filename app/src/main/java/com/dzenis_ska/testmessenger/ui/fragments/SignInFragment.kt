package com.dzenis_ska.testmessenger.ui.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navOptions
import com.dzenis_ska.testmessenger.R
import com.dzenis_ska.testmessenger.databinding.FragmentSignInBinding
import com.dzenis_ska.testmessenger.db.FBAuth
import com.dzenis_ska.testmessenger.ui.MainApp
import com.dzenis_ska.testmessenger.ui.activities.ViewModelMain
import com.dzenis_ska.testmessenger.utils.CheckNetwork

class SignInFragment : Fragment(R.layout.fragment_sign_in) {

    private var binding: FragmentSignInBinding? = null
    private val args by navArgs<SignInFragmentArgs>()
//    var timer: CountDownTimer? = null


    private val viewModelMain: ViewModelMain by activityViewModels {
        ViewModelMain.MainViewModelFactory((context?.applicationContext as MainApp).fbAuth, (context?.applicationContext as MainApp).fbFirestore)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignInBinding.bind(view)

        initClick()
        logOut()

    }



    private fun initClick() = with(binding!!) {

        etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sign()
                return@setOnEditorActionListener false
            }
            return@setOnEditorActionListener false
        }  

        bSignIn.setOnClickListener {
            sign()
        }
    }

    private fun logOut(){
        Log.d("!!!", "${args.isLogOut}")
        if(args.isLogOut) viewModelMain.logOut()
    }

    private fun sign() = with(binding!!) {
        val email = etEmail.text.toString()
        val pass = etPassword.text.toString()

        if (email.isNotEmpty()
            &&
            pass.isNotEmpty()
            &&
            email.contains('@')
            &&
            email.contains('.')
            &&
            !email.contains(' ')
            &&
            pass.length > 5
        ) {
            if(!CheckNetwork.isNetworkAvailable(context)) return
            bSignIn.isEnabled = false
            progressBar.isVisible = true
            viewModelMain.signIn(email, pass) { mess->
                Log.d("!!!db", "${mess}")
                bSignIn.isEnabled = true
                progressBar.isVisible = false
                if (mess == FBAuth.LUCKY_ENTER) {
                    val isEV = viewModelMain.isEmailVerified()
                    if (isEV != null && isEV == true) {
                        val name = viewModelMain.fa.currentUser?.displayName
                        if(name == null){
                            findNavController().navigate(R.id.enterNameFragment)
                            Toast.makeText(context, "?????????????? ??????!", Toast.LENGTH_LONG).show()
                        } else {
                            findNavController().navigate(R.id.homeFragment, null,
                                navOptions {
                                    popUpTo(R.id.signInFragment){
                                        inclusive = true
                                    }
                                })
                            Toast.makeText(context, mess, Toast.LENGTH_LONG).show()
                        }
                    }else {
                        Toast.makeText(context, "?????????????????? ???????????????? ????????!", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    when (mess){
                        FBAuth.CHANGE_EMAIL -> Toast.makeText(context, FBAuth.CHANGE_EMAIL, Toast.LENGTH_LONG)
                            .show()
                        FBAuth.ERROR_EMAIL_VERIFICATION -> Toast.makeText(context, FBAuth.ERROR_EMAIL_VERIFICATION, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            bSignIn.isEnabled = true
            progressBar.isVisible = false
            Toast.makeText(
                context,
                "???????????????? ???????????? email...",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}