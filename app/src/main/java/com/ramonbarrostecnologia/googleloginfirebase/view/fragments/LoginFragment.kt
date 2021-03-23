package com.ramonbarrostecnologia.googleloginfirebase.view.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.ramonbarrostecnologia.googleloginfirebase.R
import com.ramonbarrostecnologia.googleloginfirebase.databinding.FragmentLoginBinding
import com.ramonbarrostecnologia.googleloginfirebase.domain.AuthResult
import com.ramonbarrostecnologia.googleloginfirebase.domain.LoginData
import com.ramonbarrostecnologia.googleloginfirebase.viewModel.LoginViewModel

class LoginFragment : Fragment() {
    lateinit var gso: GoogleSignInOptions
    lateinit var mGoogleSignInClient: GoogleSignInClient

    lateinit var binding: FragmentLoginBinding
    lateinit var navController: NavController
    private val viewModel: LoginViewModel by viewModels()
    lateinit var ctx: Context;

    private val GOOGLE_LOGIN_REQUEST_CODE = 111;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.loginFragment = this
        binding.lifecycleOwner = this
        //TODO understand better how the NAVCONTROLLER works.
        navController = findNavController()
        viewModel.loginResult.observe(viewLifecycleOwner) { loginResult ->
            proccessLoginResult(loginResult)

        }
        viewModel.loginWithGoogleResult.observe(viewLifecycleOwner) { loginResult ->
            proccessLoginWithGoogleResult(loginResult)

        }
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
        gso = setGso()
        mGoogleSignInClient = setmGoogleSignInClient(requireActivity(), gso)
        viewModel.lookIfLoggedGoogle(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_REQUEST_CODE) {
            viewModel.loginWithGoogle(this, requestCode, data)
        }

    }

    /////////////////////////////////
    // METHODS
    /////////////////////////////////

    fun login() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val data = LoginData(email, password)
        Log.w("LOGIN", "LOGANDO")

        viewModel.login(data, ctx)
    }

    fun loginWithGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_REQUEST_CODE)
    }


    fun proccessLoginResult(result: AuthResult) {
        if (result.error != "") {
            Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(context, R.string.connectionStabilished, Toast.LENGTH_LONG).show()
        navController.navigate(R.id.action_loginFragment_to_appActivity)
    }

    fun proccessLoginWithGoogleResult(result: AuthResult) {
        if (result.error != "") {
            Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
            return
        }
        if (result.result == "REGISTER_IS_COMPLETED") {
            Toast.makeText(context, R.string.connectionStabilished, Toast.LENGTH_LONG).show()
            navController.navigate(R.id.action_loginFragment_to_appActivity)
        } else {
            val data = result.data
            if (data != null) {
                val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment(
                    data.name,
                    data.email,
                    data.phoneNumber,
                    data.uid,
                    data.photoUrl
                )
                Toast.makeText(context, R.string.registerWithProviderSuccess, Toast.LENGTH_LONG)
                    .show()
                navController.navigate(action)
            }
        }
    }

    fun navToRegister() {
        navController.navigate(R.id.action_loginFragment_to_registerFragment)
    }

    fun navToForgotPass() {
        navController.navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }

    fun setGso(): GoogleSignInOptions {
        return GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(requireContext().getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    fun setmGoogleSignInClient(activity: Activity, gso: GoogleSignInOptions): GoogleSignInClient {
        return GoogleSignIn.getClient(activity, gso)
    }
}

