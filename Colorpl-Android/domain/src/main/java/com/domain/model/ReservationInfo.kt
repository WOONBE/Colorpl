package com.domain.model

import java.io.Serializable

data class ReservationInfo (
    val reservationInfoId: Int = 0,
    val contentImg: String? = null,
    val title: String = "",
    val cast: String? = null,
    val category: String? = null,
    val runtime: String = "",
    val priceBySeatClass: Map<String, Int> = mapOf(),
    val schedule: Map<String, Boolean>? = null,
) : Serializable