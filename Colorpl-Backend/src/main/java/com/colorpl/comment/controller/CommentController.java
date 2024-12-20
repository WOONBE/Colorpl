package com.colorpl.comment.controller;

import com.colorpl.comment.dto.CommentDTO;
import com.colorpl.comment.dto.CommentResponse;
import com.colorpl.comment.dto.ReadCommentResponse;
import com.colorpl.comment.dto.CommentRequestDTO;
import com.colorpl.comment.service.CommentService;
import com.colorpl.global.common.exception.CommentNotFoundException;
import com.colorpl.global.common.exception.MemberNotFoundException;
import com.colorpl.global.common.exception.ReviewNotFoundException;
import com.colorpl.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final MemberService memberService;
    private final CommentService commentService;

    // 특정 리뷰의 모든 댓글 가져오기
    @GetMapping("/reviews/{reviewId}")
    @Operation(summary = "특정 리뷰의 댓글 가져오기(무한 스크롤)", description = "특정 리뷰의 댓글을 조회할 때 사용하는 API(url의 리뷰id 사용)")
    public ResponseEntity<ReadCommentResponse> getCommentsByReviewId(
            @PathVariable Long reviewId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Integer memberId = memberService.getCurrentMemberId();
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CommentDTO> commentPage = commentService.getCommentsByReviewId(memberId, reviewId, pageable);
            List<CommentDTO> comments = commentPage.getContent();

            int totalPages = (int) Math.ceil((double) comments.size() / size);
            ReadCommentResponse response = ReadCommentResponse.builder().items(comments).totalPage(totalPages).build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ReviewNotFoundException e) {
            ReadCommentResponse response = ReadCommentResponse.builder().items(Collections.emptyList()).totalPage(-1).build();
            return new ResponseEntity<>(response,HttpStatus.OK);
        }
    }

    // 리뷰에 댓글 작성
    @PostMapping("/reviews/{reviewId}")
    @Operation(summary = "특정 리뷰에 댓글 작성하기(토큰 사용)", description = "특정 리뷰의 댓글을 작성할 때 사용하는 API(url의 리뷰id 사용)")
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long reviewId, @RequestBody CommentRequestDTO commentRequestDTO) {
        try {
            Integer memberId = memberService.getCurrentMemberId();

            CommentDTO createdComment = commentService.createComment(reviewId, memberId, commentRequestDTO);
            CommentResponse response = CommentResponse.builder().commentId(createdComment.getId()).failPoint("none").build();
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (MemberNotFoundException e) {
            CommentResponse response = CommentResponse.builder().commentId(-1L).failPoint("member not found").build();
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        } catch (ReviewNotFoundException e) {
            CommentResponse response = CommentResponse.builder().commentId(-1L).failPoint("review not found").build();
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }
    }

    // 특정 댓글 수정
    @PutMapping("/reviews/{reviewId}/{commentId}")
    @Operation(summary = "특정 댓글 수정하기(토큰 사용)", description = "특정 댓글을 수정할 때 사용하는 API(url의 댓글id 사용)")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long commentId, @PathVariable Long reviewId, @RequestBody CommentRequestDTO commentRequestDTO) {
        try {
            Integer memberId = memberService.getCurrentMemberId();
            CommentDTO updatedComment = commentService.updateComment(commentId, reviewId,  memberId, commentRequestDTO);
            CommentResponse response = CommentResponse.builder().commentId(updatedComment.getId()).failPoint("none").build();
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (MemberNotFoundException e) {
            CommentResponse response = CommentResponse.builder()
                    .commentId(-1L)
                    .failPoint("Member ID가 없습니다.")
                    .build();
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        } catch (ReviewNotFoundException e) {
            CommentResponse response = CommentResponse.builder().commentId(-1L).failPoint("review가 없습니다.").build();
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        } catch (CommentNotFoundException e) {
            CommentResponse response = CommentResponse.builder().commentId(-1L).failPoint("comment가 없습니다.").build();
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }
    }

    // 특정 댓글 삭제
    @DeleteMapping("/{commentId}")
    @Operation(summary = "특정 댓글 삭제하기", description = "특정 댓글을 삭제할 때 사용하는 API(url의 댓글id 사용)")
    public ResponseEntity<CommentResponse> deleteCommentById(@PathVariable Long commentId) {
        try {
            commentService.deleteById(commentId);
            CommentResponse response = CommentResponse.builder().commentId(commentId).failPoint("none").build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (CommentNotFoundException e){
            CommentResponse response = CommentResponse.builder().commentId(-1L).failPoint("comment가 없습니다.").build();
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }

    }
}
