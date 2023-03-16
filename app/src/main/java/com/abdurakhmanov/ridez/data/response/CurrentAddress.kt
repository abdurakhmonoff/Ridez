package com.abdurakhmanov.ridez.data.response

data class CurrentAddress(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val postaladdress: String = "",
    val zipcode: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = ""
)
