package com.colorpl.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.colorpl.comment.domain.Comment;
import com.colorpl.comment.dto.CommentDTO;
import com.colorpl.global.common.exception.MemberNotFoundException;
import com.colorpl.global.common.exception.ReviewNotFoundException;
import com.colorpl.member.Member;
import com.colorpl.member.repository.MemberRepository;
import com.colorpl.review.domain.Review;
import com.colorpl.review.dto.ReviewDTO;
import com.colorpl.review.repository.ReviewRepository;
import com.colorpl.schedule.domain.Schedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void testFindAll() {
        // Given
        Review review = Review.builder().id(1L).content("Test Review").build();
        when(reviewRepository.findAll()).thenReturn(Collections.singletonList(review));

        // When
        List<ReviewDTO> result = reviewService.findAll();

        // Then
        verify(reviewRepository, times(1)).findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Test Review");
    }

    @Test
    void testGetReviews() {
        // Given
        Review review = Review.builder().id(1L).content("Test Review").build();
        Pageable pageable = PageRequest.of(0, 1);
        when(reviewRepository.findAll(pageable)).thenReturn(new org.springframework.data.domain.PageImpl<>(Collections.singletonList(review)));

        // When
        List<ReviewDTO> result = reviewService.getReviews(0, 1);

        // Then
        verify(reviewRepository, times(1)).findAll(pageable);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Test Review");
    }

    @Test
    void testFindMembersAll() {
        // Given
        Integer memberId = 1;
        Member member = Member.builder().id(memberId).schedules(new ArrayList<>()).build();
        Review review = Review.builder().id(1L).content("Test Review").build();
        Schedule schedule = Schedule.builder().review(review).build();
        member.getSchedules().add(schedule);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // When
        List<ReviewDTO> result = reviewService.findMembersAll(memberId);

        // Then
        verify(memberRepository, times(1)).findById(memberId);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Test Review");
    }

    @Test
    void testFindById() {
        // Given
        Long reviewId = 1L;
        Review review = Review.builder().id(reviewId).content("Test Review").build();
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // When
        ReviewDTO result = reviewService.findById(reviewId);

        // Then
        verify(reviewRepository, times(1)).findById(reviewId);
        assertThat(result.getContent()).isEqualTo("Test Review");
    }

    @Test
    void testCreateReview() {
        // Given
        Integer memberId = 1;
        Member member = Member.builder().id(memberId).build();
        ReviewDTO reviewDTO = ReviewDTO.builder().content("New Review").build();
        Review review = Review.builder().id(1L).content("New Review").build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        ReviewDTO result = reviewService.createReview(memberId, reviewDTO);

        // Then
        verify(memberRepository, times(1)).findById(memberId);
        verify(reviewRepository, times(1)).save(any(Review.class));
        assertThat(result.getContent()).isEqualTo("New Review");
    }

    @Test
    void testUpdateReview() {
        // Given
        Integer memberId = 1;
        Long reviewId = 1L;
        ReviewDTO reviewDTO = ReviewDTO.builder().content("Updated Review").build();
        Review review = Review.builder().id(reviewId).content("Old Review").build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReviewDTO result = reviewService.updateReview(memberId, reviewId, reviewDTO);

        // Then
        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, times(1)).save(any(Review.class));
        assertThat(result.getContent()).isEqualTo("Updated Review");
    }

    @Test
    void testDeleteById() {
        // Given
        Long reviewId = 1L;
        when(reviewRepository.existsById(reviewId)).thenReturn(true);

        // When
        reviewService.deleteById(reviewId);

        // Then
        verify(reviewRepository, times(1)).existsById(reviewId);
        verify(reviewRepository, times(1)).deleteById(reviewId);
    }
}
