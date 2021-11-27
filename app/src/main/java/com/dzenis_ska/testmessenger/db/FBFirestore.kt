package com.dzenis_ska.testmessenger.db

import android.net.Uri
import android.util.Log
import com.dzenis_ska.testmessenger.ui.MainApp
import com.dzenis_ska.testmessenger.ui.activities.ViewModelMain

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.collections.HashMap

class FBFirestore(private val mainApp: MainApp) {

    //Storage
    val st = Firebase.storage

    // CloudFirestore
    val db = Firebase.firestore

    //Authentication
    val auth = Firebase.auth

    fun cancelListenerWriting(email: Dialog) {
        val query = db.collection(ViewModelMain.USERS)
            .document(email.email)
        val registration = query.addSnapshotListener { snapshots, e -> }
        registration.remove()
    }

    fun readWriting(email: Dialog, callback: (writing: String) -> Unit) {
        db.collection(ViewModelMain.USERS)
            .document(email.email)
            .addSnapshotListener { value, error ->
                callback(value?.get("writing").toString())
            }
    }

    fun writing(curUser: User, writing: String) {
        db.collection(ViewModelMain.USERS)
            .document(curUser.email)
            .update("writing", writing)
    }

    fun renameUser(name: String, it: User, callback: (isRename: Boolean) -> Unit) {
        val myEmail = it.email
        db.collection(ViewModelMain.USERS)
            .get()
            .addOnSuccessListener {
                val list = arrayListOf<String>()
                it.documents.forEach {email->
                    list.add(email.data?.get("email").toString())
                }

                list.forEach { email->
                    Log.d("!!!changeName", "${ViewModelMain.USERS} \n ${email} \n ${ViewModelMain.USERS} \n ${myEmail}")
                    db.collection(ViewModelMain.USERS)
                        .document(email)
                        .collection(ViewModelMain.DIALOGS)
                        .document(myEmail)
                        .update("name", name)
                }
                callback(true)
            }.addOnFailureListener { callback(false) }
    }

    fun reselectImage(byteArray: ByteArray, editPhoto: Messages.MyMessage, dialog: Dialog, callback: (isRewrite: Boolean) -> Unit) {

        st.reference.child(ViewModelMain.PHOTO)
            .child(editPhoto.time)
            .delete()
            .addOnSuccessListener {
                st.reference.child(ViewModelMain.PHOTO).child(editPhoto.time).putBytes(byteArray)
                    .addOnSuccessListener {
                        getPhotoUrl(editPhoto.time) {
                            rewriteUrl(it,editPhoto, dialog){callback(it)}
                        }
                    }.addOnFailureListener {
                        callback(false)
                    }
            }.addOnFailureListener {
                callback(false)
            }
    }

    private fun rewriteUrl(uri: Uri?, editPhoto: Messages.MyMessage, dialog: Dialog, callback: (isRewrite: Boolean) -> Unit) {
        db.collection(ViewModelMain.DIALOGS)
            .document(dialog.dialogName)
            .collection(dialog.dialogName)
            .document(editPhoto.time)
            .update("photoUrl", uri.toString())
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }


    fun deletePhoto(messageForDelPhoto: Messages.MyMessage, message: Dialog, callback: (isPhotoDel: Boolean) -> Unit) {

        st.reference.child(ViewModelMain.PHOTO)
            .child(messageForDelPhoto.time)
            .delete()
            .addOnSuccessListener {
                if(messageForDelPhoto.message.isNullOrEmpty())
                db.collection(ViewModelMain.DIALOGS)
                    .document(message.dialogName)
                    .collection(message.dialogName)
                    .document(messageForDelPhoto.time)
                    .delete()
                    .addOnSuccessListener { callback(true) }
                    .addOnFailureListener { callback(false) }
                else
                    db.collection(ViewModelMain.DIALOGS)
                        .document(message.dialogName)
                        .collection(message.dialogName)
                        .document(messageForDelPhoto.time)
                        .update("photoUrl", "null")
                        .addOnSuccessListener { callback(true) }
                        .addOnFailureListener { callback(false) }

            }
            .addOnFailureListener { callback(false) }

    }

    fun deleteText(editMess: Messages.MyMessage, message: Dialog, callback: (isDelete: Boolean) -> Unit) {
        if(editMess.photoUrl.toString() == "null")
        db.collection(ViewModelMain.DIALOGS)
            .document(message.dialogName)
            .collection(message.dialogName)
            .document(editMess.time)
            .delete()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
        else
            db.collection(ViewModelMain.DIALOGS)
                .document(message.dialogName)
                .collection(message.dialogName)
                .document(editMess.time)
                .update("message", "")
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { callback(false) }

    }

    fun editText(editText: String, editMess: Messages.MyMessage?, message: Dialog, callback: (isInvite: Boolean) -> Unit) {
        db.collection(ViewModelMain.DIALOGS)
            .document(message.dialogName)
            .collection(message.dialogName)
            .document(editMess!!.time)
            .update("message", editText)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun uploadImage(biteArray: ByteArray, time: String, callback: (uri: Uri?) -> Unit) {
        st.reference.child(ViewModelMain.PHOTO).child(time).putBytes(biteArray)
            .addOnSuccessListener {
                getPhotoUrl(time) { callback(it) }
            }.addOnFailureListener {
                callback(null)
            }
    }

    private fun getPhotoUrl(time: String, callbackUri: (isUpload: Uri?) -> Unit) {
        st.reference.child(ViewModelMain.PHOTO).child(time).downloadUrl.addOnSuccessListener { url ->
            callbackUri(url)
        }.addOnFailureListener {
            callbackUri(null)
        }
    }

    fun isRead(d: Dialog) {
        val data = hashMapOf("isRead" to true)

        db.collection(ViewModelMain.DIALOGS)
            .document(d.dialogName)
            .collection(d.dialogName)
            .whereEqualTo("email", d.email)
            .get()
            .addOnSuccessListener { query ->
                query.documents.forEach {
                    db.collection(ViewModelMain.DIALOGS)
                        .document(d.dialogName)
                        .collection(d.dialogName)
                        .document(it.data?.get("time").toString())
                        .set(data, SetOptions.merge())
                }
            }
    }


    fun getListMess(message: Dialog, callback: (listMess: ArrayList<Messages.MyMessage>) -> Unit) {

        db.collection(ViewModelMain.DIALOGS)
            .document(message.dialogName)
            .collection(message.dialogName)
            .orderBy("time", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                val listMess = arrayListOf<Messages.MyMessage>()
                value?.forEach {
                    listMess.add(
                        Messages.MyMessage(
                            it.data.get("name").toString(),
                            it.data.get("email").toString(),
                            it.data.get("message").toString(),
                            it.data.get("isRead").toString().toBoolean(),
                            it.data.get("time").toString(),
                            it.data.get("photoUrl").toString()
                        )
                    )
                    Log.d("!!!getMessages", "exceptionIF: ${it.data.get("name")}")
                }
                callback(listMess)
            }
    }

    fun cancelListenerMess(message: Dialog) {
        val query = db.collection(ViewModelMain.DIALOGS)
            .document(message.dialogName)
            .collection(message.dialogName)
        val registration = query.addSnapshotListener { snapshots, e -> }
        registration.remove()
    }


    fun sendMessage(text: String, user: User, message: Dialog, photoUrl: Uri?, time: String, callback: (isSend: Boolean) -> Unit) {

        val mes = Messages.MyMessage(
            user.name,
            user.email,
            text,
            false,
            time,
            photoUrl.toString()
        )


        db.collection(ViewModelMain.DIALOGS)
            .document(message.dialogName)
            .collection(message.dialogName)
            .document(time)
            .set(mes)
            .addOnSuccessListener {
                incrementMess(message, user)
                sendTimeForSortDialogs(message, user, time)
                callback(true)
            }
            .addOnFailureListener { callback(false) }
    }

    private fun sendTimeForSortDialogs(message: Dialog, user: User, time: String) {
        db.collection(ViewModelMain.USERS)
            .document(message.email)
            .collection(ViewModelMain.DIALOGS)
            .document(user.email)
            .update("time", time)
    }

    private fun incrementMess(message: Dialog, user: User) {
        db.collection(ViewModelMain.USERS)
            .document(message.email)
            .collection(ViewModelMain.DIALOGS)
            .document(user.email)
            .update("countUnreadMess", FieldValue.increment(1))
    }

    fun decrementMess(message: Dialog, user: User) {
        db.collection(ViewModelMain.USERS)
            .document(user.email)
            .collection(ViewModelMain.DIALOGS)
            .document(message.email)
            .update("countUnreadMess", 0)
    }

    fun getDialogsList(myUser: User, callback: (listD: ArrayList<Dialog>) -> Unit) {
        val listDialogs = arrayListOf<Dialog>()
        db.collection(ViewModelMain.USERS)
            .document(myUser.email)
            .collection(ViewModelMain.DIALOGS)
            .orderBy("time", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->

                result.forEach {
                    val mes = it.data as HashMap<*, *>
                    Log.d("!!!getDialog", "exceptionIF: ${mes.get("name")}")
                    listDialogs.add(
                        Dialog(
                            mes.get("name").toString(),
                            mes.get("email").toString(),
                            mes.get("time").toString(),
                            mes.get("dialogName").toString(),
                            mes.get("countUnreadMess").toString().toInt()
                        )
                    )
                }
                callback(listDialogs)
            }
    }




    fun createDialog(myUser: User, user: User, callback: (isCreate: Boolean) -> Unit) {
        val dialogName = myUser.email + "|" + user.email

        val dialogMy = Dialog(
            myUser.name,
            myUser.email,
            System.currentTimeMillis().toString(),
            dialogName,
            0
        )
        val dialogHis = Dialog(
            user.name,
            user.email,
            System.currentTimeMillis().toString(),
            dialogName,
            0
        )

        val dialogCollection = db.collection(ViewModelMain.DIALOGS)
            .document(dialogName)
        dialogCollection
            .set(hashMapOf("dialog" to dialogName, "timestamp" to FieldValue.serverTimestamp()))
            .addOnSuccessListener {
                db.collection(ViewModelMain.USERS)
                    .document(myUser.email)
                    .collection(ViewModelMain.DIALOGS)
                    .document(user.email)
                    .set(dialogHis)
                    .addOnSuccessListener {
                        db.collection(ViewModelMain.USERS)
                            .document(user.email)
                            .collection(ViewModelMain.DIALOGS)
                            .document(myUser.email)
                            .set(dialogMy)
                            .addOnSuccessListener {
                                callback(true)
                            }.addOnFailureListener { callback(false) }
                    }.addOnFailureListener { callback(false) }
            }.addOnFailureListener { callback(false) }

    }

    fun declineUser(myEmail: String, user: User, callback: (user: User) -> Unit) {

        val mainColl = db.collection(ViewModelMain.USERS)
            .document(myEmail)

        mainColl.update("forInvite", FieldValue.arrayRemove(user)).addOnSuccessListener { task ->
            db.collection(ViewModelMain.USERS)
                .document(user.email)
                .update("invite", FieldValue.arrayRemove(myEmail))
                .addOnSuccessListener {
//                    db.collection(ViewModelMain.USERS)
//                        .document(user.email)
//                        .update("forInvite", FieldValue.arrayRemove(user))
                }
            callback(user)
        }
    }

    fun listInviting(myEmail: String, callback: (userList: MutableList<User>) -> Unit) {

        val mainCollection = db.collection(ViewModelMain.USERS)
            .document(myEmail)

        //query
        mainCollection
            .get()
            .addOnSuccessListener { documents ->
                val listInviter = mutableListOf<User>()

                try {
                    Log.d("!!!exception", "exception: ${documents.get("forInvite")}")

                    val list = documents.get("forInvite")
                    if (list != null) {
                        val l = list as List<*>
                        l.forEach {
                            it as HashMap<*, *>
                            listInviter.add(
                                User(
                                    it.get("name").toString(),
                                    it.get("email").toString(),
                                    it.get("photoUrl").toString(),
                                    it.get("emailVerified").toString().toBoolean(),
                                    it.get("uid").toString()
                                )
                            )
                        }
                        callback(listInviter)
                    } else {
                        callback(listInviter)
                    }

                } catch (e: IllegalStateException) {
                    Log.d("!!!exception", "exception: ${e}")
                }


            }.addOnFailureListener { exception ->
                Log.d("!!!isInv", "exception: ${exception}")
            }

    }

    fun getListInvite(it: User, callback: (listInvite: ArrayList<*>?) -> Unit) {
        db.collection(ViewModelMain.USERS)
            .document(it.email)
            .get()
            .addOnSuccessListener {
                val list = it.get("invite")
                if(list != null){
                    list as ArrayList<*>
                    Log.d("!!!isInv", "exception: ${list}")
                    callback(list)
                }
            }.addOnFailureListener {  callback(null) }
    }

    fun inviteUser(curUser: User, inviteEmail: String, callback: (isInvite: Boolean) -> Unit) {
        val curUserEmail = curUser.email
        if (inviteEmail == curUserEmail) {
            callback(false)
            return
        }


        db.collection(ViewModelMain.USERS)
            .document(inviteEmail)
            .collection(ViewModelMain.DIALOGS)
            .document(curUserEmail)
            .get()
            .addOnSuccessListener { doc ->

                Log.d("!!!dbqwe", "Document: Suss")
                if (doc.data?.get("email").toString() != curUserEmail) {
                    invUserStep2(inviteEmail, curUser) {
                        callback(it)
                    }
                } else {
                    callback(false)
                }
            }.addOnFailureListener {  Log.d("!!!dbqwe", "Document: Fail")}
    }

    fun invUserStep2(inviteEmail: String, curUser: User, callback: (isInvite: Boolean) -> Unit) {
        var emailTarget = ""
        val curUserEmail = curUser.email
        //query
        db.collection(ViewModelMain.USERS)
            .document(inviteEmail)
            .get()
            .addOnSuccessListener{
                val list = it.get("invite")
                var isCurUsEm = false
                if(list != null){
                    list as ArrayList<*>
                    if(list.size > 0){
                        list.forEach {inv->
                            if(inv.toString() == curUserEmail) isCurUsEm = true
                        }
                    } else {
                        inviteStep3(inviteEmail, curUser) { isInvite ->
                            callback(isInvite)
                        }
                    }

                } else {
                    inviteStep3(inviteEmail, curUser){ isInvite->
                        callback(isInvite)
                    }
                }

                if(!isCurUsEm){
                    inviteStep3(inviteEmail, curUser){ isInvite->
                        callback(isInvite)
                    }
                } else {
                    callback(false)
                }
            }
    }

    private fun inviteStep3(inviteEmail: String, curUser: User, callback: (isInvite: Boolean) -> Unit) {
        db.collection(ViewModelMain.USERS)
            .document(inviteEmail)
            .update("forInvite", FieldValue.arrayUnion(curUser))
            .addOnSuccessListener {
                db.collection(ViewModelMain.USERS)
                    .document(curUser.email)
                    .update("invite", FieldValue.arrayUnion(inviteEmail))

                callback(true)
                Log.d("!!!db", "DocumentSnapshot successfully written!")
            }.addOnFailureListener { e -> Log.w("!!!db", "Error writing document", e) }
    }

    fun getListUsers(callback: (listEmail: ArrayList<String>) -> Unit) {
        db.collection(ViewModelMain.USERS)
            .get()
            .addOnSuccessListener { query->
                val listEmail = arrayListOf<String>()
                query.forEach {
                    listEmail.add(it.data["email"].toString())
                    Log.d("!!!db", "${it.data["email"]}")
                }
                if(listEmail.size > 0) callback(listEmail)
            }
    }
}