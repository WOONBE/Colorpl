package com.domain.mapper


import com.domain.model.ReservationInfo
import timber.log.Timber

fun com.data.model.response.ResponseReservationInfo.toEntity(): ReservationInfo {
    Timber.tag("data").d(this.schedule.toString())
    return ReservationInfo(
        reservationInfoId = this.id,
        contentImg = this.posterImagePath,
        title = this.name,
        cast = this.cast,
        category = this.category,
        runtime = this.runtime,
        priceBySeatClass = this.priceBySeatClass,
        schedule = this.schedule

    )

}