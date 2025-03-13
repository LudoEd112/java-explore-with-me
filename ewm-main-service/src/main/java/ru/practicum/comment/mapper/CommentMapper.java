package ru.practicum.comment.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.user.model.User;
import ru.practicum.comment.dto.CommentInputDto;
import ru.practicum.comment.dto.CommentOutputDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public Comment toComment(CommentInputDto commentInputDto, User user, Event event) {
        return new Comment(
                null,
                commentInputDto.getComment(),
                event,
                user,
                LocalDateTime.now()
        );
    }

    public CommentOutputDto toCommentOutputDto(Comment comment) {
        EventMapper eventMapper = new EventMapper();
        return new CommentOutputDto(
                comment.getId(),
                comment.getComment(),
                comment.getCreator().getName(),
                eventMapper.toEventShortDto(comment.getEvent()),
                comment.getCreated()
        );
    }

    public List<CommentOutputDto> toCommentOutputDtoList(List<Comment> comments) {
        return comments.stream()
                .map(this::toCommentOutputDto)
                .collect(Collectors.toList());
    }
}