package com.ramonbarrostecnologia.googleloginfirebase.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.ramonbarrostecnologia.googleloginfirebase.R
import com.ramonbarrostecnologia.googleloginfirebase.databinding.FragmentForgotPasswordBinding
import com.ramonbarrostecnologia.googleloginfirebase.domain.AuthResult
import com.ramonbarrostecnologia.googleloginfirebase.domain.LoginData
import com.ramonbarrostecnologia.googleloginfirebase.viewModel.LoginViewModel

class ForgotPasswordFragment : Fragment() {

    lateinit var binding: FragmentForgotPasswordBinding
    lateinit var ctx: Context
    lateinit var navController: NavController
    val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        binding.forgotPasswordFragment = this
        binding.lifecycleOwner = this
        navController = findNavController()
        viewModel.resetPassResult.observe(viewLifecycleOwner) { resetResult ->
            proccessResetPassResult(resetResult)
        }
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    fun resetPassword(){
        val email = binding.etEmail.text.toString()
        val data = LoginData(email, "genericPassword")

        viewModel.resetPassword(data, ctx)
    }

    fun proccessResetPassResult(result: AuthResult){
        if (result.error != "") {
            Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(context, R.string.passwordReset, Toast.LENGTH_LONG).show()
        navController.navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
    }
}