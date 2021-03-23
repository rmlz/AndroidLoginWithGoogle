package com.ramonbarrostecnologia.googleloginfirebase.domain

import java.io.File

data class RegisterData (
    val email: String,
    val password: String,
    val name: String,
    val birthday: String,
    val phoneNumber: String,
    val isMarried: Boolean,
    val hasSon: Boolean,
    val uid: String?,
    val photoUrl: String,
    val photoFile: File?,
    val gender: String,
    val isCompleteRegister: Boolean
)
{
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        false,
        false,
        "",
        "",
        null,
        "",
        false)
}