package com.ramonbarrostecnologia.googleloginfirebase.interactor

import android.content.ContentValues
import android.content.Intent
import android.util.Log
import android.util.Patterns
import com.google.firebase.firestore.DocumentSnapshot
import com.ramonbarrostecnologia.googleloginfirebase.domain.AuthResult
import com.ramonbarrostecnologia.googleloginfirebase.domain.LoginData
import com.ramonbarrostecnologia.googleloginfirebase.domain.RegisterData
import com.ramonbarrostecnologia.googleloginfirebase.repository.LoginRepository

class AuthInteractor {
    val repo = LoginRepository()

    suspend fun login(data: LoginData): AuthResult {
        var result = treatLoginData(data)
        if (result.error == "") {
            result = repo.loginToFirebase(data)
        }
        return result
    }

    suspend fun loginWithGoogle(requestCode: Int, data: Intent?): AuthResult {
        var refDoc: DocumentSnapshot?
        var userData: RegisterData
        var account = repo.signInWithGoogle(requestCode, data)
        var registerResult: AuthResult
        if (account != null) {
            registerResult = repo.signInCredentials(account)
            registerResult = repo.checkIfRegisterIsComplete(registerResult)
            if (registerResult.result == "REGISTER_IS_COMPLETED") {
                return registerResult
            }
            userData = registerResult.data!!
            refDoc = repo.findFireStoreDocument(registerResult.data!!.uid)
            if (refDoc != null) {
                repo.recordOnFireStore(userData, refDoc, registerResult)
                    .also { registerResult = it }
                return registerResult
            }
        } else {
            Log.w(ContentValues.TAG, "signInResult:fail -- ACCOUNT IS NULL")
            registerResult = AuthResult()
            registerResult.error = "ACCOUNT IS NULL"
        }
        return registerResult


    }

    suspend fun register(data: RegisterData): AuthResult {
        var dataWithPhotoUrl: RegisterData
        var result = treatRegisterData(data)
        if (result.error == "") {
            val uid = data.uid
            if (uid != null) {
                result = repo.updateDataOnFireStore(uid, data, result)
                result = repo.linkPasswordLogin(result, data)
            } else {
                val ref = repo.registerToFirebase(data)
                if (ref == null) {
                    Log.w(
                        ContentValues.TAG,
                        "ERROR GETTING URL REFERENCE IN FIRESTORE FOR DOC $uid"
                    )
                    result.error = "ERROR GETTING URL REFERENCE IN FIRESTORE FOR DOC $uid"
                } else {
                    val photoUrlTask = repo.getPhotoDownloadUrl(ref, data)
                    if (photoUrlTask == null) {
                        Log.w(ContentValues.TAG, "ERROR UPLOAD TASK FIRESTORE FOR DOC $uid")
                        result.error = "ERROR UPLOAD TASK FIRESTORE FOR DOC $uid"
                    } else {
                        val photoDownloadUrl = repo.putGotUrlToRegisterData(photoUrlTask)
                        if (photoDownloadUrl == null) {
                            dataWithPhotoUrl = data
                            Log.w(
                                ContentValues.TAG,
                                "PHOTOURL IS NULL FOR USER RELATED TO DOC $uid"
                            )
                        } else {
                            dataWithPhotoUrl = data.copy(photoUrl = photoDownloadUrl)
                        }
                        val refDoc = repo.findFireStoreDocument(uid)!!
                        if (refDoc == null) {
                            Log.w(ContentValues.TAG, "ERROR FINDING DOCUMENT $uid ON FIRESTORE")
                            result.error = "ERROR FINDING DOCUMENT $uid ON FIRESTORE"
                        }
                        result = repo.recordOnFireStore(dataWithPhotoUrl, refDoc, result)
                    }
                }
            }
        }
        return result
    }

    suspend fun resetPassword(data: LoginData): AuthResult {
        var result = treatLoginData(data)
        if (result.error == "") {
            result = repo.resetPassFirebase(data)
        }
        return result
    }

    fun treatLoginData(data: LoginData): AuthResult {
        val result = AuthResult()

        if (data.email.isBlank()) {
            result.error = "EMPTY EMAIL"
            return result
        }

        if (data.password.isBlank()) {
            result.error = "EMPTY PASSWORD"
            return result
        }

        if (data.password.length < 6) {
            result.error = "PASSWORD MINIMUM LENGTH"
            return result
        }

        if (data.password.length > 15) {
            result.error = "PASSWORD MAXIMUM LENGTH"
            return result
        }

        if (!isEmailValid(data.email)) {
            result.error = "EMAIL INVALID"
            return result
        }
        return result
    }

    fun treatRegisterData(data: RegisterData): AuthResult {
        val result: AuthResult = treatLoginData(LoginData(data.email, data.password))
        val dateRegex: Regex = Regex("\\d{1,2}\\/\\d{1,2}\\/\\d{4}")
        val phoneNumbRegex: Regex = Regex("\\d{10,11}")

        if (result.error != "") {
            return result
        }

        if (data.name.isBlank()) {
            result.error = "EMPTY NAME"
            return result
        }

        if (data.name.length < 3) {
            result.error = "NOT REAL NAME"
            return result
        }

        if (data.birthday.isBlank()) {
            result.error = "EMPTY BIRTHDAY"
            return result
        }

        if (!dateRegex.matches(data.birthday)) {
            result.error = "ERROR BIRTHDAY"
            return result
        }

        if (data.phoneNumber.isBlank()) {
            result.error = "EMPTY PHONENUMBER"
            return result
        }

        if (!phoneNumbRegex.matches(data.phoneNumber)) {
            result.error = "ERROR PHONENUMBER"
            return result
        }

        if (data.photoUrl.isBlank() && data.photoFile == null) {
            result.error = "EMPTY PHOTO"
            return result
        }
        return result
    }


    // HELPER

    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
