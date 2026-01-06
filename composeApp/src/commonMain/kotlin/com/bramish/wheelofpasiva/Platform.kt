package com.bramish.wheelofpasiva

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform