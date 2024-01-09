package com.noble.sync.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.noble.sync.R
import com.noble.sync.model.User
import java.io.File

class Auth {
    companion object {
        var userExtra: User? = null
        var userPhoto: File? = null

        fun createUserWithEmailAndPassword(
            auth: FirebaseAuth,
            email: String,
            password: String
        ): Task<AuthResult> {
            return auth.createUserWithEmailAndPassword(email, password)
        }

        fun uploadUserPhoto(store: FirebaseStorage, photo: File, userUid: String): UploadTask {
            val ref = store.reference.child("avatar/$userUid")
            return ref.putFile(Uri.fromFile(photo))
        }

        fun uploadUserExtras(db: FirebaseFirestore, user: User): Task<Void> {
            return db.collection("users").document(user.uid).set(user.getHashMap())
        }

        fun deleteUserPhoto(storage: FirebaseStorage, userUid: String) {
            val ref = storage.reference.child("profile/$userUid")
            ref.delete()
        }

        fun updateUserExtra(
            db: FirebaseFirestore,
            userUid: String,
            root: View,
            onComplete: (success: Boolean?) -> Unit
        ) {
            db.collection("users").document(userUid).get().addOnCompleteListener {
                if (!it.isSuccessful) {
                    Snackbar.make(root, R.string.update_user_extra_error, Snackbar.LENGTH_LONG)
                        .show()
                } else {
                    try {
                        userExtra = it.result.toObject<User>()
                    } catch (e: Exception) {
                        Log.e("Test", e.stackTraceToString())
                    }
                }
                onComplete(it.isSuccessful)
            }
        }

        fun showInvalidAuthMessage(ctx: Context, ex: Exception, root: View) {
            val msg = when (ex) {
                is FirebaseAuthInvalidCredentialsException -> ctx.getString(R.string.invalid_credentials)
                is FirebaseAuthInvalidUserException -> ctx.getString(R.string.user_not_found)
                is FirebaseAuthUserCollisionException -> ctx.getString(R.string.email_in_use)
                else -> ctx.getString(R.string.generic_error)
            }
            Snackbar.make(root, msg, Snackbar.LENGTH_LONG).show()
        }
    }
}