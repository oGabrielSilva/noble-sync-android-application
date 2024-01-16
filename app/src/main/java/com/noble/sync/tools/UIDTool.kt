package com.noble.sync.tools

import java.util.Date
import java.util.UUID

class UIDTool {
    companion object {
        fun getUID(): String = "${Date().time}${UUID.randomUUID().toString().replace("-", "")}"
    }
}