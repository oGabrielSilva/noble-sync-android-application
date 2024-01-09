package com.noble.sync.model

import com.noble.sync.enum.Gender

class User(
    val uid: String,
    val nickname: String,
    val gender: Gender,
    val year: Int,
    val bio: String
) {
    constructor() : this("", "", Gender.OTHER, -1, "")

    fun getHashMap(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()
        hashMap["uid"] = uid
        hashMap["nickname"] = nickname
        hashMap["gender"] = gender.toString() // Assuming Gender is an enum with a toString() method
        hashMap["year"] = year
        hashMap["bio"] = bio
        return hashMap
    }
}