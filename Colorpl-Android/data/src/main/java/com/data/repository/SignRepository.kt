package com.data.repository

import com.data.model.request.RequestGoogleSignIn
import com.data.model.request.RequestSignIn
import com.data.model.request.RequestSignUp
import com.data.model.response.ResponseSignIn
import com.data.model.response.ResponseSignUp
import com.data.util.ApiResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface SignRepository {


    suspend fun signIn(
        requestSignIn: RequestSignIn
    ): Flow<ApiResult<ResponseSignIn>>

    suspend fun signOut(accessToken: String, refreshToken: String): Flow<ApiResult<Any?>>

    suspend fun signUp(
        requestSignUp: RequestSignUp,
        file : File?
    ): Flow<ApiResult<ResponseSignUp>>

    suspend fun googleSignIn(
        requestGoogleSignIn: RequestGoogleSignIn
    ): Flow<ApiResult<ResponseSignIn>>
}