package com.ramonbarrostecnologia.googleloginfirebase.domain

data class UserDataResult (
    var error: String = "",
    var userData: UserData? = null
)