package com.data.model.paging

import java.util.Date

data class Comment(
    val commentId: Int,
    val name: String,
    val uploadDate: Date,
    val lastEditDate: Date? = null,
    val content: String
)