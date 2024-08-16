package com.domain.mapper

import com.data.model.paging.Comment
import com.data.model.request.RequestCreateComment

fun Comment.toEntity(): com.domain.model.Comment {
    return com.domain.model.Comment(
        id = id,
        reviewId = reviewId,
        memberId = memberId,
        writer = writer,
        commentContent = commentContent,
        createdate = createdate,
        mycomment = mycomment
    )
}

fun String.toEntity(): RequestCreateComment {
    return RequestCreateComment(
        this
    )
}