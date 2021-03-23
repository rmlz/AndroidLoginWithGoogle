package com.ramonbarrostecnologia.googleloginfirebase.repository

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ramonbarrostecnologia.googleloginfirebase.domain.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LoginRepository {

    private val db = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun loginToFirebase(data: LoginData): AuthResult = suspendCoroutine {
        val loginResult = AuthResult()
        val operation = firebaseAuth.signInWithEmailAndPassword(data.email, data.password)
        operation.addOnCompleteListener { result ->
            if (result.isSuccessful) {
                loginResult.result = "LOGIN_FIREBASE_SUCCESS"
            } else {
                Log.w(ContentValues.TAG, "signInWithEmail:failure", result.exception)
                loginResult.error = result.exception.toString()
            }
            it.resume(loginResult) // resume is the return of suspend functions
        }
    }

    suspend fun checkIfRegisterIsComplete(registerResult: AuthResult): AuthResult = suspendCoroutine {
        var currentUser = firebaseAuth.currentUser!!
        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { fireStoreData ->
                if (fireStoreData.data != null) {
                    if (fireStoreData.data!!["completeRegister"] as Boolean) {
                        registerResult.result = "REGISTER_IS_COMPLETED"
                        registerResult.error = ""
                        Log.d("EITA", "REGISTER ALREADY COMPLETE")
                        it.resume(registerResult)
                    } else {
                        registerResult.result = ""
                        Log.d("EITA", "REGISTER NOT COMPLETED")
                        it.resume(registerResult)
                    }
                } else {
                    registerResult.result = "REGISTER_IS_NOT_COMPLETED"
                    it.resume(registerResult)
                }
            }
            .addOnFailureListener {e ->
                Log.d("EITA", "REGISTER WENT WRONG", e)
                registerResult.error = "REGISTER_WENT_WRONG"
                it.resume(registerResult)
            }

    }


    suspend fun signInWithGoogle(requestCode: Int, data: Intent?): GoogleSignInAccount = suspendCoroutine {
        if (requestCode == 111) {
            var task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            task.addOnSuccessListener { result ->
                var account: GoogleSignInAccount = result
                it.resume(account)
            }
        }
    }

    suspend fun signInCredentials(account: GoogleSignInAccount): AuthResult = suspendCoroutine {
        var registerResult = AuthResult()
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val operation = firebaseAuth.signInWithCredential(credential)
        operation
            .addOnCompleteListener { opResult ->
                val dataFromGoogle = listOf(
                    account.email,
                    account.displayName,
                    account.photoUrl.toString()
                )
                val dataFromGoogleWithoutNull =
                    transformNullInBlank(dataFromGoogle)
                if (opResult.isSuccessful) {
                    val currentUser = firebaseAuth.currentUser!!
                    val uid = currentUser.uid

                    val userData = RegisterData(
                        email = dataFromGoogleWithoutNull[0],
                        password = "",
                        name = dataFromGoogleWithoutNull[1],
                        birthday = "",
                        phoneNumber = "",
                        isMarried = false,
                        hasSon = false,
                        uid = uid,
                        photoUrl = dataFromGoogleWithoutNull[2],
                        photoFile = null,
                        gender = "",
                        isCompleteRegister = false
                    )
                    registerResult.data = userData
                    registerResult.result = "LOGIN_FIREBASE_WITH_PROVIDER_SUCCESS"
                    registerResult.error = ""
                    Log.w(ContentValues.TAG, "signInResult:success idToken=" + account.idToken)
                    Log.w(ContentValues.TAG, "signInResult:success displayName=" + account.displayName)
                    it.resume(registerResult)
                } else {
                    Log.w(ContentValues.TAG,"signInWithEmail:failure", opResult.exception)
                    registerResult.error = "${opResult.exception}"
                    it.resume(registerResult)
                }
            }
    }

    suspend fun updateDataOnFireStore(uid: String, data: RegisterData, result: AuthResult): AuthResult = suspendCoroutine {
        val userData = toUserdata(data)
        db.collection("users")
            .document(uid)
            .set(userData)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    ContentValues.TAG,
                    "DocumentSnapshot added with ID: ${documentReference}"
                )
                if (result.result == "") {
                    result.result = "REGISTER_FIREBASE_SUCCESS"
                    it.resume(result)
                }
            }
            .addOnFailureListener { res ->
                Log.w(ContentValues.TAG, "Error adding document", res)
                result.error = res.toString()
                it.resume(result)
            }
    }

    suspend fun linkPasswordLogin(result: AuthResult, data: RegisterData): AuthResult = suspendCoroutine {
        val credential = EmailAuthProvider.getCredential(data.email, data.password)
        val currentUser = firebaseAuth.currentUser!!
        currentUser.linkWithCredential(credential)
            .addOnCompleteListener { res ->
                if (res.isSuccessful) {
                    result.result = "REGISTER_FIREBASE_SUCCESS"
                    it.resume(result)
                } else {
                    result.result = ""
                    result.error = res.exception.toString()
                    it.resume(result)
                }
            }
    }

    suspend fun findFireStoreDocument (uid: String?): DocumentSnapshot? = suspendCoroutine {
        if (uid == null) {
            it.resume(null)
        } else {
            db.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener { doc ->
                    if (doc.isSuccessful) {
                        val document = doc.result
                        it.resume(document)
                    }
                }
        }
    }


    suspend fun recordOnFireStore(data: RegisterData, document: DocumentSnapshot, registerResult: AuthResult): AuthResult = suspendCoroutine {
        val userData = toUserdata(data)
        registerResult.data = data
        if (!document.exists()) {
            db.collection("users")
                .document(data.uid!!)
                .set(userData)
                .addOnSuccessListener { documentReference ->
                    Log.d(
                        ContentValues.TAG,
                        "DocumentSnapshot added with ID: ${documentReference}"
                    )
                    if (registerResult.result == "") {
                        registerResult.result = "REGISTER_FIREBASE_SUCCESS"
                        it.resume(registerResult)
                    } else {
                        it.resume(registerResult)
                    }

                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                    registerResult.error = e.toString()
                    it.resume(registerResult)
                }
        }
        else {
            Log.w(ContentValues.TAG, "DOCUMENT ALREADY EXISTS!")
            it.resume(registerResult)
        }

    }

    suspend fun registerToFirebase(data: RegisterData): StorageReference? = suspendCoroutine {
        var reference = storage.reference

        val operation = firebaseAuth.createUserWithEmailAndPassword(data.email, data.password)
        operation.addOnCompleteListener { result ->
            if (result.isSuccessful) {
                val user = result.result?.user
                val uid = user?.uid.toString()
                reference = reference.child("images").child(uid).child("$uid.jpg")
                it.resume(reference)
            } else {
                it.resume(null)
            }
        }
    }

    suspend fun getPhotoDownloadUrl(reference: StorageReference, data: RegisterData): Task<Uri>? = suspendCoroutine { cont ->
        var registerResult = AuthResult()
        var photoUrl: String;

        val uploadTask = reference.putFile(Uri.fromFile(data.photoFile))
        uploadTask
            .addOnSuccessListener {
                cont.resume(reference.downloadUrl)

            }
            .addOnFailureListener { exception ->
                cont.resume(null)
            }
    }

    suspend fun putGotUrlToRegisterData(refDownloadUrl: Task<Uri>): String? = suspendCoroutine {
        refDownloadUrl
            .addOnSuccessListener { urlRes ->
                val photoUrl = urlRes.toString()
                it.resume(photoUrl)
            }
            .addOnFailureListener { exception ->
                it.resume(null)
            }
    }

    suspend fun resetPassFirebase(data: LoginData): AuthResult = suspendCoroutine {
        val registerResult = AuthResult()
        val firebaseAuth = FirebaseAuth.getInstance()
        val operation = firebaseAuth.sendPasswordResetEmail(data.email)
        operation.addOnCompleteListener { result ->
            Log.w(ContentValues.TAG, "isResetSuccessful" + result.isSuccessful.toString())
            if (result.isSuccessful) {
                Log.w("SUCESSO SENHA", result.result.toString())
                registerResult.result = "RESETPASS_FIREBASE_SUCCESS"
            } else {
                Log.w(ContentValues.TAG, "resetPassword:failure", result.exception)
                registerResult.error = result.exception.toString()
                Log.w(ContentValues.TAG, "resetPassword:failure" + registerResult.error)
            }
            it.resume(registerResult)
        }
    }

    suspend fun getUserData(uid: String): UserDataResult = suspendCoroutine {
        var result = UserDataResult()

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null) {
                    result.error = ""
                    result.userData = doc.toObject(UserData::class.java)
                    Log.w("FIRESTORE_GET_USER", "$doc")
                    it.resume(result)
                } else {
                    Log.w("FIRESTORE_GET_USER_NULL", "$doc")
                    result.error = "NULL_DOCUMENT"
                    it.resume(result)
                }
            }
            .addOnFailureListener { exception ->
                result.error = exception.toString()
                Log.w("FSTORE_GET_USER_ERROR", result.error)
                it.resume(result)
            }
    }

    //HELPER
    private fun toUserdata(data: RegisterData): UserData {
        return UserData(
            email = data.email,
            name = data.name,
            birthday = data.birthday,
            phoneNumber = data.phoneNumber,
            isMarried = data.isMarried,
            hasSon = data.hasSon,
            photoUrl = data.photoUrl,
            gender = data.gender,
            isCompleteRegister = data.isCompleteRegister
        )
    }
    private fun transformNullInBlank(stringArray: List<String?>): List<String> {
        val notNull = stringArray.mapNotNull {
            if (it != null){
                it
            } else {
                ""
            }
        }
        return notNull
    }
}