package com.domain.usecaseimpl.feed

import androidx.paging.PagingData
import androidx.paging.map
import com.data.repository.CommentRepository
import com.data.util.ApiResult
import com.domain.mapper.toEntity
import com.domain.model.Comment
import com.domain.usecase.CommentUseCase
import com.domain.util.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class CommentUseCaseImpl @Inject constructor(private val commentRepository: CommentRepository) :
    CommentUseCase {
    override fun getComment(reviewId: Int): Flow<PagingData<Comment>> {
        return commentRepository.getPagedComment(reviewId)
            .map { pagingData ->
                pagingData.map { comment ->
                    comment.toEntity()
                }
            }
    }

    override suspend fun editComment(
        reviewId: Int,
        commentId: Int,
        commentContent: String
    ): Flow<DomainResult<Int>> =
        flow {
            commentRepository.editComment(reviewId, commentId, commentContent.toEntity())
                .collect { result ->
                    when (result) {
                        is ApiResult.Error -> {
                            Timber.tag("review").d("usecase edit ${result.exception}")
                            result.exception.printStackTrace()
                            emit(DomainResult.error(result.exception))
                        }

                        is ApiResult.Success -> {
                            Timber.tag("review").d("usecase edit ${result.data.commentId}")
                            emit(DomainResult.success(result.data.commentId))
                        }
                    }
                }
        }

    override suspend fun deleteComment(commentId: Int): Flow<DomainResult<Int>> =
        flow {
            commentRepository.deleteComment(commentId)
                .collect { result ->
                    when (result) {
                        is ApiResult.Error -> {
                            Timber.tag("review").d("usecase delete ${result.exception}")
                            result.exception.printStackTrace()
                            emit(DomainResult.error(result.exception))
                        }

                        is ApiResult.Success -> {
                            Timber.tag("review").d("usecase delete ${result.data.commentId}")
                            emit(DomainResult.success(result.data.commentId))
                        }
                    }
                }
        }

    override suspend fun createComment(
        reviewId: Int,
        commentContent: String
    ): Flow<DomainResult<Int>> =
        flow {
            commentRepository.createComment(reviewId, commentContent.toEntity())
                .collect { result ->
                    when (result) {
                        is ApiResult.Error -> {
                            Timber.tag("review").d("usecase create ${result.exception}")
                            result.exception.printStackTrace()
                            emit(DomainResult.error(result.exception))
                        }

                        is ApiResult.Success -> {
                            Timber.tag("review").d("usecase create ${result.data.commentId}")
                            emit(DomainResult.success(result.data.commentId))
                        }
                    }
                }
        }
}