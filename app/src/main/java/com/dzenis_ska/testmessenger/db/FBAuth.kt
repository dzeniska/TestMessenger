package com.dzenis_ska.testmessenger.db

import android.util.Log
import android.widget.Toast
import com.dzenis_ska.testmessenger.ui.MainApp
import com.dzenis_ska.testmessenger.ui.activities.ViewModelMain
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FBAuth(private val mainApp: MainApp) {

    //Authentication
    val auth = Firebase.auth
    // CloudFirestore
    val db = Firebase.firestore


    fun isCurUser(callback: (task: FirebaseUser?) -> Unit) =
        if (Firebase.auth.currentUser != null){
        callback(Firebase.auth.currentUser)
    }else{
        callback(null)
    }



    fun setName(name: String, callback: (task: Boolean) -> Unit){
        val user = Firebase.auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            displayName = name
        }
        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                callback(task.isSuccessful)
            createFirstCollection()
            }
    }

    fun getCurUser(): User? {

        val user = auth.currentUser
        return if (user != null) User(
            user.displayName.toString(),
            user.email.toString(),
            user.photoUrl.toString(),
            user.isEmailVerified,
            user.uid.toString()
        )
        else null
    }

    private fun createFirstCollection() {
        val user = getCurUser()
        if (user == null) {
            return
        } else {
            val docRef = db.collection(ViewModelMain.USERS)
                .document(user.email)

            docRef.set(user, SetOptions.merge())
                .addOnSuccessListener { Log.d("!!!db", "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w("!!!db", "Error writing document", e) }

            docRef.update(hashMapOf<String, Any>("timeStamp" to FieldValue.serverTimestamp()))
        }
    }



    fun signIn(email: String, password: String, callback: (task: String) -> Unit) {
        val auth = Firebase.auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendEmailVerification(task.result?.user!!)
                } else {
                    Log.d("!!!init", "else")
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        val exception = task.exception as FirebaseAuthUserCollisionException
                        if (exception.errorCode == ERROR_EMAIL_ALREADY_IN_USE) {
                            Log.d("!!!error", exception.errorCode)
                            signInWithEmail(email, password, auth) {
                                callback(it)
                            }
                        }
                    } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == ERROR_INVALID_EMAIL) {
                            callback(exception.errorCode)
                            Log.d("!!!error", exception.errorCode)
                        } else if (exception.errorCode == ERROR_WEAK_PASSWORD) {
                            callback(exception.errorCode)
                            Log.d("!!!error", exception.errorCode)
                        }
                    }
                    if (task.exception is FirebaseAuthWeakPasswordException) {
                        val exception = task.exception as FirebaseAuthWeakPasswordException
                        if (exception.errorCode == ERROR_WEAK_PASSWORD) {
                            callback(exception.errorCode)
                            Log.d("!!!error", exception.errorCode)
                        }
                    }
                }
            }.addOnFailureListener {
                callback("Exeption: ${it.message}")
                Log.d("!!!error", "Exeption: ${it.message}")
            }
    }

    private fun signInWithEmail(email: String, password: String, auth: FirebaseAuth, callback: (task: String) -> Unit) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(LUCKY_ENTER)
                    } else {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            val exception =
                                task.exception as FirebaseAuthInvalidCredentialsException
                            if (exception.errorCode == ERROR_INVALID_EMAIL) {
                                callback(exception.errorCode)
                                Log.d("!!!error", exception.errorCode)
                            } else if (exception.errorCode == ERROR_WRONG_PASSWORD) {
                                callback(exception.errorCode)
                                Log.d("!!!error", exception.errorCode)
                            }
                        } else if (task.exception is FirebaseAuthInvalidUserException) {
                            val exception = task.exception as FirebaseAuthInvalidUserException
                            if (exception.errorCode == ERROR_USER_NOT_FOUND) {
                                callback(exception.errorCode)
                                Log.d("!!!error", exception.errorCode)
                            }
                        }
                    }
                }.addOnFailureListener {
                    callback("Exeption: ${it.message}")
                    Log.d("!!!error", "Exeption: ${it.message}")
                }


    }


    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener() { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    mainApp,
                    "Проверьте почтовый ящик",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    mainApp,
                    "Error!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object{
        const val ERROR_EMAIL_ALREADY_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE"
        const val ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL"
        const val ERROR_WRONG_PASSWORD = "ERROR_WRONG_PASSWORD"
        const val ERROR_WEAK_PASSWORD = "ERROR_WEAK_PASSWORD"
        const val ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"
        const val LUCKY_ENTER = "Удачный вход!"
    }
}