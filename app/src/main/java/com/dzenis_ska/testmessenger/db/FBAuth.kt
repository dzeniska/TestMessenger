package com.dzenis_ska.testmessenger.db

import android.os.CountDownTimer
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

    init{
        Log.d("!!!signInWithEmailAndPassword1", "${Firebase.auth.currentUser}")
    }

    fun isCurUser(callback: (task: FirebaseUser?) -> Unit){
        val currUser = auth.currentUser
        if (currUser != null && currUser.isEmailVerified) {
            callback(currUser)
        } else {
            callback(null)
        }
    }

    fun setName(name: String, callback: (task: Boolean) -> Unit) {
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

    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        callback: (task: String) -> Unit
    ){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = Firebase.auth.currentUser
                    Log.d("!!!signInWithEmailAndPassword", "${user}")
                    if (user != null) {
                        if (!user.isEmailVerified) {
                            sendEmailVerification(user) {
                                callback(it)
                            }
                        } else {
                            callback(LUCKY_ENTER)
                        }
                    }
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val exception =
                            task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == ERROR_INVALID_EMAIL) {
                            callback(ERROR_INVALID_EMAIL)
                        } else if (exception.errorCode == ERROR_WRONG_PASSWORD) {
                            callback(ERROR_WRONG_PASSWORD)
                        }
                    } else if (task.exception is FirebaseAuthInvalidUserException) {
                        val exception = task.exception as FirebaseAuthInvalidUserException
                        if (exception.errorCode == ERROR_USER_NOT_FOUND) {
                            createUserWithEmailAndPassword(email, password) {
                                if (it == LUCKY_CREATE) signInWithEmailAndPassword(
                                    email,
                                    password
                                ) { sign ->
                                    callback(sign)
                                }
                            }
                            callback(exception.errorCode)
                        }
                    }
                }
            }.addOnFailureListener {
                callback("Exeption: ${it.message}")
                Log.d("!!!error", "Exeption: ${it.message}")
            }
    }

    private fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        callback: (task: String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(LUCKY_CREATE)
                } else {
                    Log.d("!!!signIn", "else")
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        val exception = task.exception as FirebaseAuthUserCollisionException
                        if (exception.errorCode == ERROR_EMAIL_ALREADY_IN_USE) {
                            callback(ERROR_EMAIL_ALREADY_IN_USE)
                        }
                    } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val exception = task.exception as FirebaseAuthInvalidCredentialsException
                        if (exception.errorCode == ERROR_INVALID_EMAIL) {
                            callback(ERROR_INVALID_EMAIL)
                        } else if (exception.errorCode == ERROR_WEAK_PASSWORD) {
                            callback(ERROR_WEAK_PASSWORD)
                        }
                    }
                    if (task.exception is FirebaseAuthWeakPasswordException) {
                        val exception = task.exception as FirebaseAuthWeakPasswordException
                        if (exception.errorCode == ERROR_WEAK_PASSWORD) {
                            callback(ERROR_WEAK_PASSWORD)
                        }
                    }
                }
            }.addOnFailureListener {
                callback("Exeption: ${it.message}")
                Log.d("!!!error", "Exeption: ${it.message}")
            }
    }

    private fun sendEmailVerification(user: FirebaseUser, callback: (task: String) -> Unit) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(CHANGE_EMAIL)
            } else {
                callback(ERROR_EMAIL_VERIFICATION)
            }
        }
    }



    companion object {
        const val ERROR_EMAIL_ALREADY_IN_USE = "ERROR_EMAIL_ALREADY_IN_USE"
        const val ERROR_INVALID_EMAIL = "ERROR_INVALID_EMAIL"
        const val ERROR_WRONG_PASSWORD = "ERROR_WRONG_PASSWORD"
        const val ERROR_WEAK_PASSWORD = "ERROR_WEAK_PASSWORD"
        const val ERROR_USER_NOT_FOUND = "ERROR_USER_NOT_FOUND"
        const val LUCKY_ENTER = "Удачный вход!"
        const val LUCKY_CREATE = "Account created!"


        const val CHANGE_EMAIL = "Проверьте почтовый ящик"
        const val ERROR_EMAIL_VERIFICATION = "Чекни свой почтовый ящик говорю!)"


    }
}