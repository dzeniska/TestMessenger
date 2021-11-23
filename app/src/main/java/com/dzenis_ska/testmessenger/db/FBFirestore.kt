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

    fun sendMessage(text: String, it: User, message: Dialog, photoUrl: Uri?, time: String, callback: (isSend: Boolean) -> Unit) {

        val mes = Messages.MyMessage(it.name, it.email, text, false, time, photoUrl.toString())
        db.collection(ViewModelMain.DIALOGS)
            .document(message.dialogName)
            .collection(message.dialogName)
            .document(time)
            .set(mes)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun getDialogsList(myUser: User, callback: (listD: ArrayList<Dialog>) -> Unit) {
        val listDialogs = arrayListOf<Dialog>()
        val getDialogCollection = db.collection(ViewModelMain.USERS)
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
                            mes.get("dialogName").toString()
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
            dialogName
        )
        val dialogHis = Dialog(
            user.name,
            user.email,
            System.currentTimeMillis().toString(),
            dialogName
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


//            val time = System.currentTimeMillis().toString()
//        dialogCollection.update(hashMapOf<String, Any>("timeStamp" to time))


//        val dialogCollection = db.collection(ViewModelMain.DIALOGS)
//            .document("Fghy1Ht3O7U8zXiKE0RY").get().addOnSuccessListener { doc->
//                Log.d("!!!isInvite", "exceptionIF: ${doc.get("timeStamp")}")
//                val stamp: Timestamp =
//                stamp.timestamp = doc.get("timeStamp")
//
//
//                val date = Date(stamp.timestamp)
//                println(date)
//
//
//            }

    }

    fun declineUser(myEmail: String, user: User, callback: (user: User) -> Unit) {
        Log.d("!!!isInvite", "exceptionIF: ${myEmail} _ ${user.uid}")
        val mainColl = db.collection(ViewModelMain.USERS)
            .document(myEmail)

        mainColl.update("forInvite", FieldValue.arrayRemove(user)).addOnCompleteListener { task ->
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

    fun inviteUser(curUser: User, inviteEmail: String, callback: (isInvite: Boolean) -> Unit) {
        if (inviteEmail == curUser.email) {
            callback(false)
            return
        }
        val curUserEmail = curUser.email

        db.collection(ViewModelMain.USERS)
            .document(inviteEmail)
            .collection(ViewModelMain.DIALOGS)
            .document(curUserEmail)
            .get()
            .addOnSuccessListener { doc ->
                Log.d("!!!db", "Document2: ${doc.data?.get("email")} _ ${curUserEmail}")

                if (doc.data?.get("email").toString() != curUserEmail) {
                    invUserStep2(inviteEmail, curUser) {
                        callback(it)
                    }
                } else {
                    callback(false)
                }
            }
    }

    fun invUserStep2(inviteEmail: String, curUser: User, callback: (isInvite: Boolean) -> Unit) {
        var emailTarget = ""
        val mainCollection = db.collection(ViewModelMain.USERS)
        //query
        mainCollection.whereEqualTo("email", inviteEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    emailTarget = document.get("email").toString()
                    Log.d("!!!db", "Document: ${emailTarget} _ ${document.data}")
                }
                if (emailTarget.isNotEmpty() && emailTarget != curUser?.email) {
                    mainCollection
                        .document(inviteEmail)
//            .update(hashMapOf<String, Any>(myEmail to inviteEmail))
                        .update("forInvite", FieldValue.arrayUnion(curUser))
//                        .set(curUser!!)
                        .addOnSuccessListener {
                            callback(true)
                            Log.d("!!!db", "DocumentSnapshot successfully written!")
                        }.addOnFailureListener { e -> Log.w("!!!db", "Error writing document", e) }
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("!!!isInv", "exception: ${exception}")
            }
    }


//    fun getDialog2(myUser: User, callback: (listD: ArrayList<String>) -> Unit) {
//        val listDialogs = arrayListOf<String>()
//        val getDialogCollection = db.collection(ViewModelMain.USERS)
//            .document(myUser.email)
//            .collection(ViewModelMain.DIALOGS)
//            .addSnapshotListener { value, error ->
//                value?.forEach {
//                    it.data.forEach { data ->
//                        listDialogs.add(data.value.toString())
//                        Log.d("!!!getDialog", "exceptionIF: ${data.value}")
//                    }
//                    callback(listDialogs)
//                }
//            }
//    }
}