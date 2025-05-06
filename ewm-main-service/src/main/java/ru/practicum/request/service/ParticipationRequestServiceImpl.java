package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.user.model.User;
import ru.practicum.user.dao.UserRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.ParticipationRequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.ParticipationRequestStatus;
import ru.practicum.request.dao.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository participationRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAllParticipationRequests(Long userId) {
        List<ParticipationRequest> requests = participationRequestRepository.findByRequesterId(userId);
        if (requests.isEmpty()) {
            return new ArrayList<>();
        }
        return participationRequestMapper.toParticipationRequestDtoList(requests);
    }

    @Override
    public ParticipationRequestDto createParticipationRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = {} не существует." + eventId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id = {} не существует." + userId));
        ParticipationRequest requestValid =
                participationRequestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (requestValid != null || event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictException("Пользователь является инициатором события или уже подал заявку на участие.");
        }
        if (event.getParticipantLimit().equals(event.getConfirmedRequests()) && event.getParticipantLimit() != 0) {
            throw new ConflictException("На данное мероприятие больше нет мест.");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие еще не было опубликовано.");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setEvent(event);
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());

        if (event.getParticipantLimit() == 0
                || (!event.getRequestModeration() && event.getParticipantLimit() > event.getConfirmedRequests())) {
            request.setStatus(ParticipationRequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else if (!event.getRequestModeration()
                && event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            request.setStatus(ParticipationRequestStatus.REJECTED);
        } else {
            request.setStatus(ParticipationRequestStatus.PENDING);
        }

        participationRequestRepository.save(request);
        return participationRequestMapper.toParticipationRequestDto(request);
    }

    @Override
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id = {} не существует." + userId));
        ParticipationRequest request = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявки с id = {} не существует." + requestId));
        if (!request.getRequester().equals(user)) {
            throw new ConflictException("Отменить заявку может только пользователь, иницировавший её.");
        }

        request.setStatus(ParticipationRequestStatus.CANCELED);

        Event event = request.getEvent();
        if (Boolean.TRUE.equals(event.getRequestModeration())) {
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        }

        return participationRequestMapper.toParticipationRequestDto(request);
    }
}