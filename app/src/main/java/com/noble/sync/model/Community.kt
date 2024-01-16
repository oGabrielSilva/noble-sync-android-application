package com.noble.sync.model

import android.graphics.Bitmap
import com.noble.sync.enum.Visibility
import com.noble.sync.tools.UIDTool

class Community(
    var uid: String,
    var name: String,
    var icon: String?,
    var visibility: Visibility,
    var description: String,
    var createdBy: String,
    var staffs: List<String>,
    var members: List<String>
) {
    var iconBitmap: Bitmap? = null

    constructor() : this("", "", "", Visibility.PRIVATE, "", "", listOf(), listOf())

    constructor(
        name: String,
        icon: String,
        visibility: Visibility,
        description: String,
        createdBy: String,
    ) : this(UIDTool.getUID(), name, icon, visibility, description, createdBy, listOf(), listOf())

    fun getHashMap(): HashMap<String, Any> {
        val communityMap = HashMap<String, Any>()
        communityMap["uid"] = uid
        communityMap["name"] = name
        communityMap["icon"] = icon ?: ""
        communityMap["visibility"] = visibility.toString()
        communityMap["description"] = description
        communityMap["createdBy"] = createdBy
        communityMap["staffs"] = staffs
        communityMap["members"] = members
        return communityMap
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Community

        if (uid != other.uid) return false
        if (name != other.name) return false
        if (icon != other.icon) return false
        if (visibility != other.visibility) return false
        if (description != other.description) return false
        if (createdBy != other.createdBy) return false
        if (members != other.members) return false
        return staffs == other.staffs
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + icon.hashCode()
        result = 31 * result + visibility.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + createdBy.hashCode()
        result = 31 * result + staffs.hashCode()
        result = 31 * result + members.hashCode()
        return result
    }
}