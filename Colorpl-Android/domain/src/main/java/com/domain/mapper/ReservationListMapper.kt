package com.domain.mapper


import com.domain.model.ReservationInfo

fun List<com.data.model.response.ResponseReservationInfo>.toEntity(): List<ReservationInfo> {
    return this.map {
        ReservationInfo(
            reservationInfoId = it.id,
            contentImg = it.posterImagePath,
            title = it.name,
            category = it.category ?: "카테고리",
            runtime = it.runtime,
            priceBySeatClass = it.priceBySeatClass
        )
    }
}