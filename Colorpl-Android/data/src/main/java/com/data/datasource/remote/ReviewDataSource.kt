package com.data.datasource.remote

import com.data.model.paging.ResponsePagedFeed
import com.data.model.request.RequestReviewEdit
import com.data.model.response.ResponseReviewCreate

import com.data.model.response.ResponseReviewDetail
import com.data.model.response.ResponseReviewEdit
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ReviewDataSource {
    suspend fun deleteReview(reviewId: Int): ResponseReviewEdit
    suspend fun editReview(reviewId: Int, requestReviewEdit: RequestReviewEdit): ResponseReviewEdit
    suspend fun createReview(
        review: MultipartBody.Part?,
        request: RequestBody
    ): ResponseReviewCreate

    suspend fun getReviewDetail(
        reviewId: Int
    ): ResponseReviewDetail

    suspend fun addEmpathize(
        reviewId: Int
    ): ResponseReviewEdit

    suspend fun deleteEmpathize(
        reviewId: Int
    ): ResponseReviewEdit


}