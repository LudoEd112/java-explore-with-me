package ru.practicum.event.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.category.dto.CategoryOutputDto;
import ru.practicum.event.model.EventState;
import ru.practicum.user.dto.UserDtoWithoutEmail;
import ru.practicum.user.model.User;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventNewDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventMapper {

    public Event toEvent(EventNewDto eventRequestDto, User initiator, Category category) {
        Event event = new Event();
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setConfirmedRequests(0);
        event.setAnnotation(eventRequestDto.getAnnotation());
        event.setDescription(eventRequestDto.getDescription());
        event.setEventDate(eventRequestDto.getEventDate());
        event.setLocation(eventRequestDto.getLocation());
        event.setPaid(eventRequestDto.getPaid());
        event.setParticipantLimit(eventRequestDto.getParticipantLimit());
        event.setRequestModeration(eventRequestDto.getRequestModeration());
        event.setTitle(eventRequestDto.getTitle());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        return event;
    }

    public EventShortDto toEventShortDto(Event event) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setId(event.getId());
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setCategory(new CategoryOutputDto(event.getCategory().getId(), event.getCategory().getName()));
        eventShortDto.setConfirmedRequests(event.getConfirmedRequests());
        eventShortDto.setEventDate(event.getEventDate());
        eventShortDto.setInitiator(new UserDtoWithoutEmail(event.getInitiator().getId(), event.getInitiator().getName()));
        eventShortDto.setPaid(event.getPaid());
        eventShortDto.setTitle(event.getTitle());
        eventShortDto.setViews(0);
        return eventShortDto;
    }

    public EventFullDto toEventFullDto(Event event) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(event.getId());
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setCategory(new CategoryOutputDto(event.getCategory().getId(), event.getCategory().getName()));
        eventFullDto.setConfirmedRequests(event.getConfirmedRequests());
        eventFullDto.setCreatedOn(event.getCreatedOn());
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setEventDate(event.getEventDate());
        eventFullDto.setInitiator(new UserDtoWithoutEmail(event.getInitiator().getId(), event.getInitiator().getName()));
        eventFullDto.setLocation(event.getLocation());
        eventFullDto.setPaid(event.getPaid());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setPublishedOn(event.getPublishedOn());
        eventFullDto.setRequestModeration(event.getRequestModeration());
        eventFullDto.setState(event.getState());
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setViews(event.getViews());
        return eventFullDto;
    }

    public List<EventShortDto> toEventShortDtoList(List<Event> events) {
        return events.stream()
                .map(this::toEventShortDto)
                .collect(Collectors.toList());
    }

    public List<EventFullDto> toEventFullDtoList(List<Event> events) {
        return events.stream()
                .map(this::toEventFullDto)
                .collect(Collectors.toList());
    }
}