package com.ramonbarrostecnologia.googleloginfirebase.domain

data class UserData (
    val email: String,
    val name: String,
    val birthday: String,
    val phoneNumber: String,
    val isMarried: Boolean,
    val hasSon: Boolean,
    var photoUrl: String,
    var gender: String = "Masculino",
    var isCompleteRegister: Boolean = false
) {
    constructor() : this("","","","",false,false,"","", false)
}