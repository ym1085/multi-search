package com.multi.posts.controller.api;

import com.multi.posts.constant.StatusEnum;
import com.multi.posts.dto.request.CommentRequestDto;
import com.multi.posts.service.CommentService;
import com.multi.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1")
@RestController
public class CommentApiController {

    private final CommentService commentService;

    /**
     * 댓글, 대댓글 저장
     *
     * @description 대댓글의 경우는 parentId를 Front 영역에서 받아 DB에 등록하는 형식을 진행
     * 만약 parentId가 없다면 일반 댓글 등록, 그렇지 않다면 대댓글 등록으로 간주 하였습니다.
     */
    @PostMapping("/post/{postId}/comments")
    public ResponseEntity<ApiResponse<Integer>> saveComment(@RequestBody @Valid CommentRequestDto commentRequestDto,
                                                            @PathVariable("postId") Long postId,
                                                            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errorMessage = bindingResult.getFieldErrors()
                    .stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest().body(new ApiResponse<>(StatusEnum.BAD_REQUEST, errorMessage));
        }

        if (postId == null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(StatusEnum.BAD_REQUEST, StatusEnum.COULD_NOT_FOUND_POST_ID.getMessage()));
        }
        commentRequestDto.setMemberId(1L);
        commentRequestDto.setPostId(20L);

        int successId = commentService.saveComment(commentRequestDto);

        ResponseEntity<ApiResponse<Integer>> responseEntity;
        if (successId > 0) {
            ApiResponse<Integer> message = new ApiResponse<>(StatusEnum.OK, StatusEnum.SUCCESS_SAVE_COMMENT.getMessage(), successId);
            responseEntity = ResponseEntity.ok(message);
        } else {
            ApiResponse<Integer> message = new ApiResponse<>(StatusEnum.INTERNAL_SERVER_ERROR, StatusEnum.COULD_NOT_SAVE_COMMENT.getMessage(), successId);
            responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
        return responseEntity;
    }

    /**
     * 댓글, 대댓글 삭제
     * @param commentRequestDto 댓글 삭제 : commentId, parentId 기반 삭제, 대댓글 삭제 : commentId 기반 삭제
     * @return
     */
    @DeleteMapping("/post/comments")
    public ResponseEntity<ApiResponse<Integer>> deleteCommentById(CommentRequestDto commentRequestDto) {
        int successId = commentService.deleteCommentById(commentRequestDto);

        ApiResponse<Integer> message;
        HttpStatus status;
        if (successId > 0) {
            message = new ApiResponse<>(StatusEnum.OK, StatusEnum.SUCCESS_DELETE_COMMENT.getMessage(), successId);
            status = HttpStatus.OK;
        } else {
            message = new ApiResponse<>(StatusEnum.INTERNAL_SERVER_ERROR, StatusEnum.COULD_NOT_DELETE_COMMENT.getMessage(), successId);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(status).body(message);
    }

    /**
     * 댓글 수정
     * @param commentRequestDto
     * @return
     */
    @PutMapping("/post/comments")
    public ResponseEntity<ApiResponse<Integer>> updateCommentById(@Valid CommentRequestDto commentRequestDto,
                                                                     BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errorMessage = bindingResult.getFieldErrors()
                    .stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest().body(new ApiResponse<>(StatusEnum.BAD_REQUEST, errorMessage));
        }

        // Todo: 해당 게시글 권한이 있는지 확인 필요 -> 추후.. security

        int successId = commentService.updateCommentById(commentRequestDto);
        ApiResponse<Integer> message;
        HttpStatus status;
        if (successId > 0) {
            message = new ApiResponse<>(StatusEnum.OK, StatusEnum.SUCCESS_UPDATE_COMMENT.getMessage(), successId);
            status = HttpStatus.OK;
        } else {
            message = new ApiResponse<>(StatusEnum.INTERNAL_SERVER_ERROR, StatusEnum.COULD_NOT_UPDATE_COMMENT.getMessage(), successId);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(status).body(message);
    }
}
