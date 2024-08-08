package com.domain.model

data class MemberSearch(
    val followerCount: Int,
    val followingCount: Int,
    val memberId: Int,
    val nickname: String,
    val profileImage: String,
    val reviewCount: Int
)
