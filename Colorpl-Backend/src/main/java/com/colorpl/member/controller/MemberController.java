package com.colorpl.member.controller;

import com.colorpl.global.common.exception.BusinessException;
import com.colorpl.global.common.exception.EmailNotFoundException;
import com.colorpl.global.common.exception.MemberNotFoundException;
import com.colorpl.member.Member;
import com.colorpl.member.dto.FindMemberResponse;
import com.colorpl.member.dto.FollowCountDTO;
import com.colorpl.member.dto.MemberDTO;
import com.colorpl.member.dto.SignInRequest;
import com.colorpl.member.dto.SignInResponse;
import com.colorpl.member.service.BlackListService;
import com.colorpl.member.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final BlackListService blackListService;

    @Autowired
    public MemberController(MemberService memberService, BlackListService blackListService) {
        this.memberService = memberService;
        this.blackListService = blackListService;
    }

    @PostMapping("/register/Oauth")
    @Operation(summary = "Oauth 회원가입", description = "Oauth 회원 가입을 진행하는 API, multipart 이미지가 필요 없음")
    public ResponseEntity<MemberDTO> registerOauthMember(@RequestPart MemberDTO memberDTO) {
        Member member = memberService.registerOauthMember(memberDTO);
        return ResponseEntity.ok(MemberDTO.toMemberDTO(member));
    }

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "회원 가입을 진행하는 API")
    public ResponseEntity<MemberDTO> registerMember(
        @RequestPart("memberDTO") MemberDTO memberDTO,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        Member member = memberService.registerMember(memberDTO, profileImage);
        return ResponseEntity.ok(MemberDTO.toMemberDTO(member));
    }


    @PostMapping("/sign-in")
    @Operation(summary = "로그인", description = "로그인을 진행하는 API")
    public ResponseEntity<SignInResponse> signIn(@RequestBody SignInRequest signInRequest) {
        SignInResponse memberDTO = memberService.signIn(signInRequest.getEmail(), signInRequest.getPassword());
        return ResponseEntity.ok(memberDTO);
    }


//    @PutMapping
//    @Operation(summary = "멤버 수정", description = "로그인 된 멤버 정보를 수정하는 API")
//    public ResponseEntity<MemberDTO> updateMember(@RequestBody MemberDTO memberDTO) {
//        Integer memberId = memberService.getCurrentMemberId();
//
//        Member updatedMember = memberService.updateMemberInfo(memberId, memberDTO);
//        return ResponseEntity.ok(MemberDTO.toMemberDTO(updatedMember));
//    }

    @PutMapping
    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "멤버 수정", description = "로그인 된 멤버 정보를 수정하는 API")
    public ResponseEntity<MemberDTO> updateMember(
        @RequestPart("memberDTO") MemberDTO memberDTO,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        Integer memberId = memberService.getCurrentMemberId();

        Member updatedMember = memberService.updateMemberInfo(memberId, memberDTO,profileImage);
        return ResponseEntity.ok(MemberDTO.toMemberDTO(updatedMember));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "멤버 정보 조회", description = "로그인한 멤버의 정보를 조회하는 API")
    public ResponseEntity<MemberDTO> findMemberById() {
        try {
            Integer memberId = memberService.getCurrentMemberId();

            MemberDTO memberDTO = memberService.findMemberById(memberId);
            return ResponseEntity.ok(memberDTO);
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    // 이메일로 멤버 조회
    @GetMapping("/email/{email}")
    @Operation(summary = "email로 멤버 조회", description = "email로 멤버를 조회하는 API")
    public ResponseEntity<MemberDTO> findMemberByEmail(@PathVariable String email) {
        try {
            MemberDTO memberDTO = memberService.findMemberByEmail(email);
            return ResponseEntity.ok(memberDTO);
        } catch (EmailNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


//    // 나의 리뷰 조회
//    @GetMapping("/{memberId}/reviews")
//    public ResponseEntity<List<Review>> getMyReviews(@PathVariable Integer memberId) {
//        List<Review> reviews = memberService.getMyReviews(memberId);
//        return new ResponseEntity<>(reviews, HttpStatus.OK);
//    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "모든 멤버 조회", description = "모든 멤버를 조회하는 API")
    public ResponseEntity<List<MemberDTO>> retrieveAllMembers() {
        List<Member> members = memberService.getAllUsers();
        List<MemberDTO> memberDTOs = members.stream()
                .map(MemberDTO::toMemberDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(memberDTOs);
    }

    @PostMapping("/sign-out")
    @Operation(summary = "로그아웃", description = "로그인 된 사용자의 로그 아웃을 진행하는 API, Refresh Token을 BlackList 처리")
    public ResponseEntity<Void> signOut(@RequestHeader("Refresh-Token") String refreshToken) {
        try {
            blackListService.signOut(refreshToken);
            return ResponseEntity.noContent().build();
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/follow/{followId}")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "팔로우", description = "로그인 된 사용자가 다른 멤버를 팔로우하는 API")
    public ResponseEntity<String> followMember(@PathVariable Integer followId) {
        Integer memberId = memberService.getCurrentMemberId();

        String followMember = memberService.followMember(memberId, followId);
        return ResponseEntity.ok(followMember);
    }

    @DeleteMapping("/unfollow/{followId}")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "언팔로우", description = "로그인 된 사용자가 다른 멤버의 팔로우를 취소하는 API")
    public ResponseEntity<String> unfollowMember(@PathVariable Integer followId) {
        Integer memberId = memberService.getCurrentMemberId();

        String unfollowMember = memberService.unfollowMember(memberId, followId);
        return ResponseEntity.status(HttpStatus.OK).body(unfollowMember);
    }

    @GetMapping("/followers")
    @Operation(summary = "팔로워 조회", description = "로그인 된 사용자의 팔로워를 조회하는 API")
    public ResponseEntity<List<MemberDTO>> getFollowers() {
        Integer memberId = memberService.getCurrentMemberId();

        List<MemberDTO> followers = memberService.getFollowers(memberId)
            .stream()
            .map(MemberDTO::toMemberDTO)
            .toList();
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following")
    @Operation(summary = "팔로우 조회", description = "로그인 된 사용자의 팔로우를 조회하는 API")
    public ResponseEntity<List<MemberDTO>> getFollow() {
        Integer memberId = memberService.getCurrentMemberId();

        List<MemberDTO> following = memberService.getFollowing(memberId)
            .stream()
            .map(MemberDTO::toMemberDTO)
            .toList();
        return ResponseEntity.ok(following);
    }

    @GetMapping("/followers/count")
    @Operation(summary = "팔로워 수 조회", description = "로그인 된 사용자의 팔로워 수를 조회하는 API")
    public ResponseEntity<FollowCountDTO> getFollowersCount() {
        Integer memberId = memberService.getCurrentMemberId();

        FollowCountDTO followersCount = memberService.getFollowersCount(memberId);
        return ResponseEntity.ok(followersCount);
    }

    @GetMapping("/following/count")
    @Operation(summary = "팔로우 수 조회", description = "로그인 된 사용자의 팔로우 수를 조회하는 API")
    public ResponseEntity<FollowCountDTO> getFollowCount() {
        Integer memberId = memberService.getCurrentMemberId();

        FollowCountDTO followingCount = memberService.getFollowingCount(memberId);
        return ResponseEntity.ok(followingCount);
    }

//    @GetMapping("/search/{nickname}")
//    @Operation(summary = "닉네임으로 멤버 검색", description = "특정 닉네임을 가진 모든 멤버를 조회하는 API")
//    public ResponseEntity<List<MemberDTO>> getMembersByNickname(@PathVariable String nickname) {
//        List<MemberDTO> members = memberService.findMembersByNickname(nickname);
//        return ResponseEntity.ok(members);
//    }


    @GetMapping("/search/{nickname}")
    @Operation(summary = "닉네임으로 멤버 검색", description = "닉네임으로 멤버를 검색하고 해당 멤버의 정보를 조회하는 API")
    public ResponseEntity<List<FindMemberResponse>> findMembersByNickname(@PathVariable String nickname) {
        List<FindMemberResponse> members = memberService.findMembersByNickname(nickname);
        return new ResponseEntity<>(members, HttpStatus.OK);
    }


//
////------------------------관리자용--------------------------//
//    @PutMapping("/{memberId}")
//    @Operation(summary = "특정 멤버 수정", description = "특정 멤버의 정보를 수정하는 API, 관리자용")
//    public ResponseEntity<MemberDTO> updateMember(@PathVariable Integer memberId, @RequestBody MemberDTO memberDTO) {
//        Member updatedMember = memberService.updateMemberInfo(memberId, memberDTO);
//        return ResponseEntity.ok(MemberDTO.toMemberDTO(updatedMember));
//    }



    @PutMapping("/{memberId}")
    @Operation(summary = "특정 멤버 수정", description = "특정 멤버의 정보를 수정하는 API, 관리자용")
    public ResponseEntity<MemberDTO> updateMember(
        @PathVariable Integer memberId,
        @RequestPart("memberDTO") MemberDTO memberDTO,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        Member updatedMember = memberService.updateMemberInfo(memberId, memberDTO, profileImage);
        return ResponseEntity.ok(MemberDTO.toMemberDTO(updatedMember));
    }


    // ID로 멤버 조회
    @GetMapping("/{memberId}")
    @Operation(summary = "memberId로 멤버 조회", description = "memberId로 멤버를 조회하는 API, 관리자용 조회")
    public ResponseEntity<MemberDTO> findMemberByIdForAdmin(@PathVariable Integer memberId) {
        try {
            MemberDTO memberDTO = memberService.findMemberById(memberId);
            return ResponseEntity.ok(memberDTO);
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }



    @GetMapping("/{memberId}/followers")
    @Operation(summary = "특정 멤버의 팔로워 조회", description = "특정 멤버의 팔로워를 조회하는 API, 관리자용")
    public ResponseEntity<List<MemberDTO>> getFollowers(@PathVariable Integer memberId) {
        List<MemberDTO> followers = memberService.getFollowers(memberId)
            .stream()
            .map(MemberDTO::toMemberDTO)
            .toList();
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{memberId}/following")
    @Operation(summary = "특정 멤버의 팔로우 조회", description = "특정 멤버의 팔로우를 조회하는 API,관리자용")
    public ResponseEntity<List<MemberDTO>> getFollow(@PathVariable Integer memberId) {
        List<MemberDTO> following = memberService.getFollowing(memberId)
            .stream()
            .map(MemberDTO::toMemberDTO)
            .toList();
        return ResponseEntity.ok(following);
    }

    @PostMapping("/{memberId}/follow/{followId}")
    @Operation(summary = "팔로우", description = "특정 멤버가 다른 멤버를 팔로우하는 API, 관리자용")
    public ResponseEntity<String> followMember(@PathVariable Integer memberId, @PathVariable Integer followId) {
        String followMember = memberService.followMember(memberId, followId);
        return ResponseEntity.ok(followMember);
    }


    @DeleteMapping("/{memberId}/unfollow/{followId}")
    @Operation(summary = "언팔로우", description = "특정 멤버가 다른 멤버의 팔로우를 취소하는 API,관리자용")
    public ResponseEntity<String> unfollowMember(@PathVariable Integer memberId, @PathVariable Integer followId) {
        String unfollowMember = memberService.unfollowMember(memberId, followId);
        return ResponseEntity.status(HttpStatus.OK).body(unfollowMember);
    }



    @GetMapping("/{memberId}/followers/count")
    @Operation(summary = "팔로워 수 조회", description = "특정 멤버의 팔로워 수를 조회하는 API,관리자용")
    public ResponseEntity<FollowCountDTO> getFollowersCount(@PathVariable Integer memberId) {
        FollowCountDTO followersCount = memberService.getFollowersCount(memberId);
        return ResponseEntity.ok(followersCount);
    }

    @GetMapping("/{memberId}/following/count")
    @Operation(summary = "팔로우 수 조회", description = "특정 멤버의 팔로우 수를 조회하는 API,관리자용")
    public ResponseEntity<FollowCountDTO> getFollowCount(@PathVariable Integer memberId) {
        FollowCountDTO followingCount = memberService.getFollowingCount(memberId);
        return ResponseEntity.ok(followingCount);
    }
}
