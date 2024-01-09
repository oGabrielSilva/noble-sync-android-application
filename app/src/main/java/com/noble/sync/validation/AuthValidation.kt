package com.noble.sync.validation

import java.time.LocalDate

class AuthValidation {
    companion object {
        private val currentYear = LocalDate.now().year
        val minYear = currentYear - 65
        val maxYear = currentYear - 14
    }

    private val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")

    fun nameIsValid(name: String): Boolean {
        return name.length >= 2
    }

    fun bioIsValid(bio: String): Boolean {
        return bio.length <= 200
    }

    fun emailIsValid(email: String): Boolean {
        return email.matches(emailRegex)
    }

    fun passwordIsValid(password: String): Boolean {
        return password.length >= 8
    }

    fun yearIsValid(year: Int): Boolean {
        return year in minYear..maxYear
    }
}