package com.ramonbarrostecnologia.googleloginfirebase.viewModel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.ramonbarrostecnologia.googleloginfirebase.R
import com.ramonbarrostecnologia.googleloginfirebase.domain.AuthResult
import com.ramonbarrostecnologia.googleloginfirebase.domain.LoginData
import com.ramonbarrostecnologia.googleloginfirebase.domain.RegisterData
import com.ramonbarrostecnologia.googleloginfirebase.interactor.AuthInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(val app: Application): AndroidViewModel(app), CoroutineScope {

    override val coroutineContext = Dispatchers.Main
    private val interactor = AuthInteractor()

    val loginResult = MutableLiveData<AuthResult>()
    val registerResult = MutableLiveData<AuthResult>()
    val resetPassResult = MutableLiveData<AuthResult>()
    val loginWithGoogleResult = MutableLiveData<AuthResult>()

    fun login(data: LoginData, ctx: Context){

        launch {
            val result = interactor.login(data)
            if (result.error != "") {
                result.error = screenTextError(ctx, result.error)
            } else {
                result.error = ""
                result.result = "Success"
            }
            loginResult.value = result
        }
    }

    fun loginWithGoogle(fragment: Fragment, requestCode: Int, data: Intent?) {
        launch {
            val result = interactor.loginWithGoogle(requestCode, data)
            if (result.error != "") {
                result.error = screenTextError(fragment.requireContext(), result.error)
            } else {
                result.error = ""
            }
            loginWithGoogleResult.value = result
        }
    }

    fun lookIfLoggedGoogle(fragment: Fragment) {
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(fragment.requireContext())
        if (account != null) {
            Log.d(ContentValues.TAG, "TEMOS UMA ACCOUNT AQUI = " +account.displayName)
        } else  {
            Log.d(ContentValues.TAG, "NÃO TEMOS UMA ACCOUNT AQUI")
        }

    }

    fun register(data: RegisterData, ctx: Context){
        launch {
            val result = interactor.register(data)
            if (result.error != "") {
                result.error = screenTextError(ctx, result.error)
            } else {
                result.error = ""
                result.result = "Success"
            }
            registerResult.value = result
        }
    }

    fun resetPassword(data: LoginData, ctx: Context){
        launch {
            val result = interactor.resetPassword(data)
            if (result.error != "") {
                result.error = screenTextError(ctx, result.error)
            } else {
                result.error = ""
                result.result = "Success"
            }
            resetPassResult.value = result
        }
    }

    fun screenTextError(ctx: Context, err: String): String {
        val errTxt: String = when (err) {
            "EMPTY EMAIL" -> ctx.getString(R.string.emptyEmail)
            "EMPTY PASSWORD" -> ctx.getString(R.string.emptyPassword)
            "PASS MINIMUM LENGTH" -> ctx.getString(R.string.passwordMinimumLimit)
            "PASS MAXIMUM LENGTH" -> ctx.getString(R.string.passwordMaximumLimit)
            "EMAIL INVALID" -> ctx.getString(R.string.invalidEmail)
            "EMPTY NAME" -> ctx.getString(R.string.emptyName)
            "NOT REAL NAME" -> ctx.getString(R.string.notRealName)
            "EMPTY BIRTHDAY" -> ctx.getString(R.string.emptyBirthday)
            "ERROR BIRTHDAY" -> ctx.getString(R.string.errorBirthday)
            "EMPTY PHONENUMBER" -> ctx.getString(R.string.emptyPhoneNumber)
            "ERROR PHONENUMBER" -> ctx.getString(R.string.errorPhoneNumber)
            "EMPTY PHOTO" -> ctx.getString(R.string.noPhotoTaken)

            else -> err
        }
        return errTxt
    }

}