package com.data.repository

import com.data.model.response.ReservationInfo
import com.data.model.response.ResponseShowSchedules
import com.data.util.ApiResult
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    suspend fun getReservationAllShows(filters: Map<String, String>): Flow<ApiResult<List<ReservationInfo>>>

    suspend fun getReservationShows(showDetailId: Int): Flow<ApiResult<ReservationInfo>>

    suspend fun getReservationSchedule(showDetailId: Int, date: String): Flow<ApiResult<List<ResponseShowSchedules>>>

}