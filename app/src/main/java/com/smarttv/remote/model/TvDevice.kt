package com.smarttv.remote.model

data class TvDevice(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 6467,
    val serviceName: String = "",
    val isPaired: Boolean = false
)
