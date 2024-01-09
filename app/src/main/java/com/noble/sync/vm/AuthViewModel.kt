package com.noble.sync.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.noble.sync.enum.Gender
import com.noble.sync.validation.AuthValidation
import java.io.File

class AuthViewModel : ViewModel() {
    private val nameData = MutableLiveData("")
    private val nicknameData = MutableLiveData("")
    private val emailData = MutableLiveData("")
    private val bioData = MutableLiveData("")
    private val passwordData = MutableLiveData("")
    private val yearData = MutableLiveData(AuthValidation.maxYear)
    private val genderData = MutableLiveData(Gender.MALE)

    private val userImageData = MutableLiveData<File>()

    val name: LiveData<String> = nameData
    val nickname: LiveData<String> = nicknameData
    val email: LiveData<String> = emailData
    val bio: LiveData<String> = bioData
    val password: LiveData<String> = passwordData
    val year: LiveData<Int> = yearData
    val gender: LiveData<Gender> = genderData
    val userImage: LiveData<File> = userImageData

    fun updateName(name: String) {
        nameData.value = name
    }

    fun updateNickname(nick: String) {
        nicknameData.value = nick
    }

    fun updateEmail(email: String) {
        emailData.value = email
    }

    fun updatePassword(pass: String) {
        passwordData.value = pass
    }

    fun updateYear(year: Int) {
        yearData.value = year
    }

    fun updateGender(g: Gender) {
        genderData.value = g
    }

    fun updateUserImage(file: File?) {
        userImageData.value = file
    }

    fun updateBio(bio: String) {
        bioData.value = bio
    }
}