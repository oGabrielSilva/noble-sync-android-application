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

    fun isEqualTo(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (uid != other.uid) return false
        if (nickname != other.nickname) return false
        if (gender != other.gender) return false
        if (year != other.year) return false
        return bio == other.bio
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + nickname.hashCode()
        result = 31 * result + gender.hashCode()
        result = 31 * result + year
        result = 31 * result + bio.hashCode()
        return result
    }


}