package org.github.amanbutnot.csvtoecr

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform