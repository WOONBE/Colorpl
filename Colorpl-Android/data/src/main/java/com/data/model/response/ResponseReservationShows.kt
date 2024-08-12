package com.data.model.response

data class ResponseReservationShows(
    val items: List<ResponseReservationInfo>
)


data class ResponseReservationInfo(
    val id: Int,
    val apiId: String?,
    val name: String,
    val cast: String?,
    val runtime: String,
    val priceBySeatClass: Map<String, Int>,
    val posterImagePath: String,
    val area: String,
    val category: String?,
    val state: String?,
    val seats: List<Seat>?,
    val hall: String?,
    val schedule: Map<String, Boolean>?,
)


data class PriceBySeatClass(
    val gradeR: Int?,
    val gradeS: Int?,
    val gradeA: Int?,
    val gradeB: Int?,
)

data class Seat(
    val row: Int,
    val col: Int,
    val grade: String,
)