package com.colorpl.member.dto;

import com.colorpl.member.Member;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindMemberResponse {
    private Integer memberId;
    private String profileImage;
    private String nickname;
    private int followerCount;
    private int followingCount;
    private int reviewCount;
    @JsonProperty("isFollowing")
    private boolean isFollowing;

    public static  FindMemberResponse toFindMemberResponse(Member member,boolean isFollowing,int reviewCount) {
        int followerCount = member.getFollowerList().size();
        int followingCount = member.getFollowingList().size();

        return FindMemberResponse.builder()
            .memberId(member.getId())
            .profileImage(member.getProfile())
            .nickname(member.getNickname())
            .followerCount(followerCount)
            .followingCount(followingCount)
            .reviewCount(reviewCount)
            .isFollowing(isFollowing)
            .build();
    }
}


