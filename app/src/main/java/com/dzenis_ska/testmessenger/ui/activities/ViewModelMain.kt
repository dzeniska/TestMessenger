package com.dzenis_ska.testmessenger.ui.activities

import android.net.Uri
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

    fun cancelListenerWriting(args: UserNameFragmentArgs){
        fbFirestore.cancelListenerWriting(args.message)
    }

    fun readWriting(args: UserNameFragmentArgs, callback: (writing: String) -> Unit) {
        fbFirestore.readWriting(args.message){
            callback(it)
        }
    }

    fun writing(writing: String) {
        getCurUser()?.let { fbFirestore.writing(it, writing)}
    }


    fun reselectImage(byteArray: ByteArray, editMess: Messages.MyMessage, args: UserNameFragmentArgs, callback: (isRewrite: Boolean?) -> Unit) {
        getCurUser()?.let { fbFirestore.reselectImage(byteArray, editMess, args.message){isRIm-> callback(isRIm)} }
    }


    fun deletePhoto(messageForDelPhoto: Messages.MyMessage, args: UserNameFragmentArgs, callback: (isPhotoDel: Boolean) -> Unit) {
        getCurUser()?.let { fbFirestore.deletePhoto(messageForDelPhoto, args.message){callback(it)} }
    }

    fun deleteText(editMess: Messages.MyMessage, args: UserNameFragmentArgs, callback: (isDelete: Boolean) -> Unit) {
        getCurUser()?.let { fbFirestore.deleteText(editMess, args.message){callback(it)} }
    }

    fun editText(editText: String, editMess: Messages.MyMessage?, args: UserNameFragmentArgs, callback: (isEdit: Boolean) -> Unit) {
        getCurUser()?.let { fbFirestore.editText(editText, editMess, args.message){callback(it)} }
    }

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

    fun clearListMess(){
        _listMess.value = arrayListOf()
    }

    fun cancelListenerMess(args: UserNameFragmentArgs) {
        fbFirestore.cancelListenerMess(args.message)
    }

    fun sendMessage(text: String, args: UserNameFragmentArgs, photoUrl: Uri?, time: String, callback: (isSend: Boolean) -> Unit) {
        getCurUser()?.let {
            fbFirestore.sendMessage(text, it, args.message, photoUrl, time) { isSend -> callback(isSend) }
        }
    }

    fun decrementMess(message: Dialog){
        getCurUser()?.let {
            fbFirestore.decrementMess(message, it)
        }
    }

    fun getDialogsList() {
        getCurUser()?.let {
            fbFirestore.getDialogsList(it) {
                _listDialogs.value = it
            }
        }
    }

    fun clearDialogList(){
        _listDialogs.value = arrayListOf()
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

    fun getListInvite(callback: (listInvite: ArrayList<*>?) -> Unit) {
        getCurUser()?.let{ fbFirestore.getListInvite(it){lInv-> callback(lInv)} }
    }

    fun clearInvUsersList(){
        _invUsers.value = mutableListOf()
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
        auth.signIn(email, password) {
            callback(it)
        }
    }

    fun isEmailVerified() = auth.auth.currentUser?.isEmailVerified


    fun setName(name: String, callback: (name: String?, status: Boolean?) -> Unit) {
        auth.setName(name) {
            getCurUser()?.let{user-> fbFirestore.renameUser(name, user){callback(name, it)} }
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

        //isWriting
        const val WRITING = "writing..."
        const val DELETING = "deleting..."
        const val STOP = "STOP"
    }
}

