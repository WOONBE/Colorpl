package com.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.domain.model.Comment
import com.domain.model.Feed
import com.domain.usecase.CommentUseCase
import com.domain.usecase.FeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getPagedFeedUseCase: FeedUseCase,
    private val getPagedCommentUseCase: CommentUseCase
) : ViewModel() {
    private val _pagedFeed = MutableStateFlow<PagingData<Feed>?>(null)
    val pagedFeed = _pagedFeed
    private val _pagedComment = MutableStateFlow<PagingData<Comment>?>(null)
    val pagedComment = _pagedComment

    fun getFeed() {
        viewModelScope.launch {
            getPagedFeedUseCase().cachedIn(viewModelScope).collectLatest { pagedData ->
                _pagedFeed.value = pagedData
            }
        }
    }

    fun getComment(feedId: Int) {
        viewModelScope.launch {
            getPagedCommentUseCase(feedId).cachedIn(viewModelScope).collectLatest { pagedData ->
                _pagedComment.value = pagedData
            }
        }
    }
}