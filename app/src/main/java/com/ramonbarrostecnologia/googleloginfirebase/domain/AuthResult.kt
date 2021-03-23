package com.ramonbarrostecnologia.googleloginfirebase.domain

data class AuthResult (
    var result: String = "",
    var error: String = "",
    var data: RegisterData? = null
)