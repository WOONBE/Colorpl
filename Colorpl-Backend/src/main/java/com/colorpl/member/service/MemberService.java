package com.colorpl.member.service;

import static com.colorpl.member.dto.MemberDTO.toMemberDTO;

import com.colorpl.global.common.exception.BusinessException;
import com.colorpl.global.common.exception.EmailAlreadyExistsException;
import com.colorpl.global.common.exception.EmailNotFoundException;
import com.colorpl.global.common.exception.MemberNotFoundException;
import com.colorpl.global.common.exception.MemberRequestNotMatchException;
import com.colorpl.global.common.exception.Messages;
import com.colorpl.global.config.TokenProvider;
import com.colorpl.member.Member;
import com.colorpl.member.MemberRefreshToken;
import com.colorpl.member.dto.MemberDTO;
import com.colorpl.member.dto.SignInResponse;
import com.colorpl.member.repository.MemberRefreshTokenRepository;
import com.colorpl.member.repository.MemberRepository;
import com.colorpl.review.domain.Review;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final MemberRefreshTokenRepository memberRefreshTokenRepository;

    //등록시 이메일 중복 여부 체크하는 로직 추후에 작성
    @Transactional
    public Member registerMember(MemberDTO memberDTO) {

        if (memberRepository.findByEmail(memberDTO.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        Member member = Member.toMember(memberDTO, passwordEncoder);
        return memberRepository.save(member);
    }

    @Transactional
    public SignInResponse signIn(String email, String password) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(MemberRequestNotMatchException::new);

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new MemberRequestNotMatchException();
        }

        String accessToken = tokenProvider.createAccessToken(String.format("%s:%s", member.getId(), member.getType()));	// token -> accessToken
        String refreshToken = tokenProvider.createRefreshToken();	// 리프레시 토큰 생성
        // 리프레시 토큰이 이미 있으면 토큰을 갱신하고 없으면 토큰을 추가
        memberRefreshTokenRepository.findById(member.getId())
            .ifPresentOrElse(
                it -> it.updateRefreshToken(refreshToken),
                () -> memberRefreshTokenRepository.save(new MemberRefreshToken(member, refreshToken))
            );
        return new SignInResponse(member.getEmail(), member.getType(), accessToken, refreshToken);
    }
    //멤버 정보 업데이트
    @Transactional
    public Member updateMemberInfo(Integer memberId, MemberDTO memberDTO) {
        Member existingMember = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버입니다."));

        existingMember.updateMember(Member.toMember(memberDTO, passwordEncoder), passwordEncoder);
        return memberRepository.save(existingMember);
    }


    //모든 멤버 조회
    @Transactional
    public List<Member> getAllUsers() {
        return memberRepository.findAll();
    }


    @Transactional
    public MemberDTO findMemberById(Integer memberId) {
        Member existingMember = memberRepository.findById(memberId)
            .orElseThrow(MemberNotFoundException::new);

        return toMemberDTO(existingMember);
    }

    @Transactional
    public MemberDTO findMemberByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new EmailNotFoundException();
        }

        Member existingMember = memberRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(Messages.MEMBER_NOT_FOUND));

        return toMemberDTO(existingMember);
    }

    //리뷰 조회는 querydsl
//    @Transactional
//    public List<Review> getMyReviews(Integer memberId) {
//        // 리뷰 엔티티가 Member와 연관된다고 가정
//        Member member = memberRepository.findById(memberId)
//            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버입니다."));
//        return member.getReviews();  // Member 엔티티에 getReviews() 메서드 추가
//    }
    //결제 내역 조회는 완성된 이후에


}