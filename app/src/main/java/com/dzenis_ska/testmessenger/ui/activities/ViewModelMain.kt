package com.dzenis_ska.testmessenger.ui.activities

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.dzenis_ska.testmessenger.db.*
import com.dzenis_ska.testmessenger.ui.fragments.UserNameFragmentArgs
import java.lang.IllegalArgumentException

class ViewModelMain(private val auth: FBAuth, private val fbFirestore: FBFirestore) : ViewModel() {


    var currentUser: User? = null

    private val _listMess = MutableLiveData<ArrayList<Messages.MyMessage>>()
    val listMess: LiveData<ArrayList<Messages.MyMessage>> = _listMess

    private val _invUsers = MutableLiveData<MutableList<User>>()
    val invUsers: LiveData<MutableList<User>> = _invUsers

    private val _listDialogs = MutableLiveData<ArrayList<Dialog>>()
    val listDialogs: LiveData<ArrayList<Dialog>> = _listDialogs

    val db = fbFirestore.db


    val fa = auth.auth

    fun uploadImage(biteArray: ByteArray, time: String, callback: (uri: Uri?) -> Unit) {
        fbFirestore.uploadImage(biteArray, time){uri->
            callback(uri)
        }
    }


    fun isRead(d: Dialog) {
        fbFirestore.isRead(d)
    }

    fun getListMess(args: UserNameFragmentArgs) {
        fbFirestore.getListMess(args.message) {
            _listMess.value = it
        }
    }

    fun cancelListenerMess(args: UserNameFragmentArgs) {
        fbFirestore.cancelListenerMess(args.message)
    }

    fun sendMessage(text: String, args: UserNameFragmentArgs, photoUrl: Uri?, time: String, callback: (isSend: Boolean) -> Unit) {
        getCurUser()?.let {
            fbFirestore.sendMessage(text, it, args.message, photoUrl, time) { isSend -> callback(isSend) }
        }
    }

    fun getDialogsList() {
        getCurUser()?.let {
            fbFirestore.getDialogsList(it) {
                _listDialogs.value = it
            }
        }
    }

    fun createDialog(user: User, callback: (isCreate: Boolean) -> Unit) {
        getCurUser()?.let { myUser ->
            fbFirestore.createDialog(myUser, user) {
                callback(it)
            }
        }
    }

    fun declineUser(user: User) {
        getCurUser()?.let { myUser ->
            fbFirestore.declineUser(myUser.email, user) {
                val users = _invUsers.value
                val indexToDelete = users?.indexOf(it)
                if (indexToDelete != -1 && indexToDelete != null) {
                    users.removeAt(indexToDelete)
                    _invUsers.value = users!!
                }
            }
        }
    }

    fun inviteUser(inviteEmail: String, callback: (isInvite: Boolean) -> Unit) {
        val user = getCurUser()

        if (user != null) {
            fbFirestore.inviteUser(user, inviteEmail) { isB ->
                callback(isB)
            }
        } else {
            callback(false)
        }
    }

    fun listInviting() {
        getCurUser()?.let {

            fbFirestore.listInviting(it.email) {
                _invUsers.value = it
            }
        }
    }


    //authentication

    fun getCurUser(): User? {
//        currentUser = auth.getCurUser()
        return auth.getCurUser()
    }

    fun logOut() {
        auth.auth.signOut()
    }

    fun signIn(email: String, password: String, callback: (status: String?) -> Unit) {
        Log.d("!!!init", "$password")
        auth.signIn(email, password) {
            callback(it)
        }
    }

    fun isEmailVerified() = auth.auth.currentUser?.isEmailVerified


    fun setName(name: String, callback: (name: String?, status: Boolean?) -> Unit) {
        auth.setName(name) {
            callback(name, it)
        }
    }


    class MainViewModelFactory(private val auth: FBAuth, private val fbFirestore: FBFirestore) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ViewModelMain::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ViewModelMain(auth, fbFirestore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel Class")
        }
    }

    companion object {
        const val NEW_USER = "NEW_USER"
        const val USERS = "USERS"
        const val DIALOGS = "DIALOGS"
        const val PHOTO = "PHOTO"
    }
}

