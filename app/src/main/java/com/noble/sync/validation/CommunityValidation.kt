package com.noble.sync.validation

class CommunityValidation {
    companion object {
        const val descriptionMaxLength = 5000
    }

    fun nameIsValid(s: String): Boolean = s.length >= 2
    fun descriptionIsValid(s: String): Boolean = s.length <= descriptionMaxLength
}