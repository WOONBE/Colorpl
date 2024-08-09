package com.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.data.api.safeApiCall
import com.data.datasource.FeedDataSource
import com.data.model.paging.Feed
import com.data.util.ApiResult
import timber.log.Timber

class FeedPagingSource(
    private val feedDataSource: FeedDataSource,
    private val type: Boolean? = ALL
) :
    PagingSource<Int, Feed>() {
    override fun getRefreshKey(state: PagingState<Int, Feed>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Feed> {
        val nextPage = params.key ?: 0
        val response = if (type == ALL) {
            feedDataSource.getFeed(nextPage, params.loadSize)
        } else {
            feedDataSource.getMyFeedData(nextPage, params.loadSize)
        }
        return when (val result =
            safeApiCall { response }) {
            is ApiResult.Success -> {
                Timber.tag("pager").d("${result.data}")
                val response = result.data.items
                Timber.tag("pager").d("${response}")

                LoadResult.Page(
                    data = response,
                    prevKey = if (nextPage == 0) null else nextPage - 1,
                    nextKey = if (response.isEmpty() || result.data.totalPage == nextPage) null else nextPage + 1
                )
            }

            is ApiResult.Error -> {
                Timber.e(result.exception, "Error loading feed data for page $nextPage")
                LoadResult.Error(result.exception)
            }
        }
    }

    companion object {
        const val ALL = true
        const val MY = false
    }
}