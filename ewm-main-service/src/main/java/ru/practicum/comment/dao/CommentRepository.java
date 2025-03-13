package ru.practicum.comment.dao;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.user.model.User;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEvent(Event event, PageRequest pageRequest);

    List<Comment> findByCreatorAndEvent(User creator, Event event, PageRequest pageRequest);

    List<Comment> findAllByCreatorIdAndEventIdAndCommentContainingIgnoreCase(Long creatorId,
                                                                             Long eventId,
                                                                             String comment,
                                                                             PageRequest request);
}