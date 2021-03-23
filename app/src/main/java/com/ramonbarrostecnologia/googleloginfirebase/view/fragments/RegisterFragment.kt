package com.ramonbarrostecnologia.googleloginfirebase.view.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.dhaval2404.imagepicker.ImagePicker
import com.ramonbarrostecnologia.googleloginfirebase.R
import com.ramonbarrostecnologia.googleloginfirebase.databinding.FragmentRegisterBinding
import com.ramonbarrostecnologia.googleloginfirebase.domain.AuthResult
import com.ramonbarrostecnologia.googleloginfirebase.domain.RegisterData
import com.ramonbarrostecnologia.googleloginfirebase.view.picassoTransform.CirclePicasso
import com.ramonbarrostecnologia.googleloginfirebase.viewModel.LoginViewModel
import com.squareup.picasso.Picasso
import java.io.File

class RegisterFragment : Fragment() {
    val args: RegisterFragmentArgs by navArgs()
    lateinit var ctx: Context
    lateinit var binding: FragmentRegisterBinding
    lateinit var navController: NavController
    private val viewModel: LoginViewModel by viewModels()
    lateinit var photoFile: File;
    lateinit var photoUrl: Uri;
    private val profilePhotoWidth = 360
    private val profilePhotoHeight = 360

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etNameRegister.setText(args.name)
        binding.etEmailRegister.setText(args.email)
        binding.etPhoneRegister.setText(args.phoneNumber)
        if (args.photoUrl != null) {
            Picasso.with(requireContext())
                .load(args.photoUrl)
                .resize(profilePhotoWidth, profilePhotoHeight)
                .transform(CirclePicasso())
                .into(binding.ivPhoto)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding.registerFragment = this
        binding.lifecycleOwner = this
        navController = findNavController()

        viewModel.registerResult.observe(viewLifecycleOwner) { registerResult ->
            proccessRegisterResult(registerResult)
        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        ctx = context

    }

    fun dispatchGetPictureIntent() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            photoUrl = data!!.data!!
            Picasso.with(requireContext())
                .load(photoUrl)
                .resize(profilePhotoWidth, profilePhotoHeight)
                .transform(CirclePicasso())
                .into(binding.ivPhoto)
            //You can get File object from intent
            photoFile = ImagePicker.getFile(data)!!

            //You can also get File Path from intent
            val filePath: String = ImagePicker.getFilePath(data)!!
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), R.string.error_task_cancelled, Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun register() {
        val uid = args.uid
        val email = binding.etEmailRegister.text.toString()
        val password = binding.etPasswordRegister.text.toString()
        val name = binding.etNameRegister.text.toString()
        val birthday = binding.etBirthdayRegister.text.toString()
        val phoneNumber = binding.etPhoneRegister.text.toString()
        val isMarried: Boolean = binding.swMarried.isChecked
        val hasSon: Boolean = binding.swHasSon.isChecked
        val newPhotoFile = if (this::photoFile.isInitialized) photoFile else null
        var newPhotoUrl = ""
        if (args.photoUrl != null) {
            newPhotoUrl = args.photoUrl!!
        } else {
            newPhotoUrl = if (this::photoUrl.isInitialized) photoUrl.toString() else ""
        }
        val gender: String = binding.spGender.selectedItem.toString()
        val isCompletedRegister = true

        val data = RegisterData(
            email,
            password,
            name,
            birthday,
            phoneNumber,
            isMarried,
            hasSon,
            uid,
            newPhotoUrl,
            newPhotoFile,
            gender,
            isCompletedRegister
        )
        viewModel.register(data, ctx)
    }

    fun proccessRegisterResult(result: AuthResult) {
        if (result.error != "") {
            Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(context, R.string.registerSuccess, Toast.LENGTH_LONG).show()
        navController.navigate(R.id.action_registerFragment_to_loginFragment)
    }
}
