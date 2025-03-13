package ru.practicum.comment.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentInputDto;
import ru.practicum.comment.dto.CommentOutputDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class CommentControllerPrivate {

    private final CommentService commentService;

    @GetMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentOutputDto> getAllComments(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long eventId,
                                                 @RequestParam(defaultValue = "0") int offset,
                                                 @RequestParam(defaultValue = "10") int limit) {
        return commentService.getAllComments(userId, eventId, offset, limit);
    }

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentOutputDto createComment(@RequestBody @Validated CommentInputDto commentInputDto,
                                          @PathVariable @Positive Long userId,
                                          @PathVariable @Positive Long eventId) {
        return commentService.createComment(commentInputDto, userId, eventId);
    }

    @PatchMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentOutputDto updateComment(@RequestBody @Validated CommentInputDto commentInputDto,
                                          @PathVariable @Positive Long userId,
                                          @PathVariable @Positive Long commentId) {
        return commentService.updateComment(commentInputDto, userId, commentId);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long userId,
                              @PathVariable @Positive Long commentId) {
        commentService.deleteComment(userId, commentId);
    }

    @GetMapping("/events/{eventId}/comments/search")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentOutputDto> searchComments(@PathVariable Long userId,
                                                 @PathVariable Long eventId,
                                                 @RequestParam @NotBlank String comment,
                                                 @RequestParam(defaultValue = "0") @PositiveOrZero Integer offset,
                                                 @RequestParam(defaultValue = "10") @Positive Integer limit) {
        return commentService.searchComments(userId, eventId, comment, offset, limit);
    }
}