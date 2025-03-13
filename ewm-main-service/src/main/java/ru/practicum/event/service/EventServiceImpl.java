package ru.practicum.event.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventNewDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.EventUpdateDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.StateAction;
import ru.practicum.user.model.User;
import ru.practicum.user.dao.UserRepository;
import ru.practicum.category.model.Category;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.dao.LocationRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.ParticipationRequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.ParticipationRequestStatus;
import ru.practicum.request.dao.ParticipationRequestRepository;
import ru.practicum.stat.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final EntityManager entityManager;
    private final StatsService statsService;
    private final EventMapper eventMapper;
    private final ParticipationRequestMapper participationRequestMapper;

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    private static final long DEFAULT_YEARS_ADDITION = 20;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllEvents(Long userId, int from, int size) {
        int startPage = from > 0 ? (from / size) : 0;
        Pageable pageable = PageRequest.of(startPage, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);
        if (events.isEmpty()) {
            return new ArrayList<>();
        }
        return eventMapper.toEventShortDtoList(events);
    }

    @Override
    public EventFullDto createEvent(Long userId, EventNewDto eventRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id = {} не существует." + userId));
        Long catId = eventRequestDto.getCategory();
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new ValidationException("Категории с id = {} не существует." + catId));
        Location location = eventRequestDto.getLocation();
        locationRepository.save(location);

        if (eventRequestDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата и время намеченного события не соответствует требованиям.");
        }

        Event event = eventMapper.toEvent(eventRequestDto, user, category);
        eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = {} не существует." + eventId));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь не является инициатором этого события.");
        }
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, EventUpdateDto eventUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id = {} не существует." + userId));
        Event oldEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = {} не существует." + eventId));
        if (!oldEvent.getInitiator().getId().equals(user.getId())) {
            throw new ValidationException("Редактирование доступно только пользователю, иницировавшему событие.");
        }
        if (oldEvent.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Редактирование недоступно для опубликованного события, либо статус не существует.");
        }
        if (eventUpdateDto.getEventDate() != null
                && eventUpdateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Дата и время намеченного события не соответствует требованиям.");
        }
        if (eventUpdateDto.getCategory() != null) {
            Long catId = eventUpdateDto.getCategory();
            Category category = categoryRepository.findById(catId)
                    .orElseThrow(() -> new ValidationException("Категории с id = {} не существует." + catId));
            oldEvent.setCategory(category);
        }
        Optional.ofNullable(eventUpdateDto.getTitle()).ifPresent(oldEvent::setTitle);
        Optional.ofNullable(eventUpdateDto.getAnnotation()).ifPresent(oldEvent::setAnnotation);
        Optional.ofNullable(eventUpdateDto.getDescription()).ifPresent(oldEvent::setDescription);
        Optional.ofNullable(eventUpdateDto.getEventDate()).ifPresent(oldEvent::setEventDate);
        Optional.ofNullable(eventUpdateDto.getLocation()).ifPresent(oldEvent::setLocation);
        Optional.ofNullable(eventUpdateDto.getParticipantLimit()).ifPresent(oldEvent::setParticipantLimit);
        Optional.ofNullable(eventUpdateDto.getPaid()).ifPresent(oldEvent::setPaid);
        Optional.ofNullable(eventUpdateDto.getRequestModeration()).ifPresent(oldEvent::setRequestModeration);

        if (eventUpdateDto.getStateAction() != null) {
            if (eventUpdateDto.getStateAction() == StateAction.SEND_TO_REVIEW) {
                oldEvent.setState(EventState.PENDING);
            } else if (eventUpdateDto.getStateAction() == StateAction.CANCEL_REVIEW) {
                oldEvent.setState(EventState.CANCELED);
            }
        }
        return eventMapper.toEventFullDto(oldEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByEventId(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id = {} не существует." + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = {} не существует." + eventId));
        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictException("Пользователь не является инициатором этого события.");
        }
        List<ParticipationRequest> requests = participationRequestRepository.findByEventId(eventId);
        if (requests.isEmpty()) {
            return new ArrayList<>();
        }
        return participationRequestMapper.toParticipationRequestDtoList(requests);
    }

    @Override
    public Map<String, List<ParticipationRequestDto>> approveRequests(Long userId,
                                                                      Long eventId,
                                                                      EventRequestStatusUpdateRequest requestUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с id = {} не существует." + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = {} не существует." + eventId));

        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictException("Пользователь не является инициатором этого события.");
        }

        List<ParticipationRequest> requests = participationRequestRepository
                .findRequestByIdIn(requestUpdateDto.getRequestIds());

        if (Boolean.TRUE.equals(event.getRequestModeration())
                && event.getParticipantLimit().equals(event.getConfirmedRequests())
                && event.getParticipantLimit() != 0
                && requestUpdateDto.getStatus() == ParticipationRequestStatus.CONFIRMED) {
            throw new ConflictException("Лимит заявок на участие в событии исчерпан.");
        }

        boolean verified = requests.stream().allMatch(r -> r.getEvent().getId().equals(eventId));
        if (!verified) {
            throw new ConflictException("Список запросов не относятся к одному событию.");
        }

        Map<String, List<ParticipationRequestDto>> requestMap = new HashMap<>();

        if (requestUpdateDto.getStatus() == ParticipationRequestStatus.REJECTED) {
            processRejectedRequests(requests, requestMap);
        } else {
            processConfirmedRequests(event, requests, requestMap);
        }

        return requestMap;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAllByAdmin(List<Long> users,
                                            List<String> states,
                                            List<Long> categories,
                                            String rangeStart,
                                            String rangeEnd,
                                            int from,
                                            int size) {
        LocalDateTime start = (rangeStart != null)
                ? LocalDateTime.parse(rangeStart, FORMATTER)
                : LocalDateTime.now();
        LocalDateTime end = (rangeEnd != null)
                ? LocalDateTime.parse(rangeEnd, FORMATTER)
                : LocalDateTime.now().plusYears(20);

        PageRequest pageRequest = PageRequest.of(from / size, size);
        if (start.isAfter(end)) {
            throw new ValidationException("Временной промежуток задан неверно.");
        }
        List<User> usersList;
        if (users == null || users.isEmpty()) {
            usersList = userRepository.findAll();
            if (usersList.isEmpty()) {
                return new ArrayList<>();
            }
        } else {
            usersList = userRepository.findByIdInOrderByIdAsc(users, pageRequest);
            if (usersList.size() != users.size()) {
                throw new ValidationException("Список пользователей передан неверно.");
            }
        }
        List<EventState> eventStates;
        if (states == null || states.isEmpty()) {
            eventStates = List.of(EventState.PUBLISHED, EventState.CANCELED, EventState.PENDING);
        } else {
            try {
                eventStates = states.stream().map(EventState::valueOf).toList();
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Недопустимое значение статуса: " + e.getMessage());
            }
        }
        List<Category> categoriesList;
        if (categories == null) {
            categoriesList = categoryRepository.findAll();
            if (categoriesList.isEmpty()) {
                return new ArrayList<>();
            }
        } else {
            categoriesList = categoryRepository.findByIdInOrderByIdAsc(categories, pageRequest);
            if (categoriesList.size() != categories.size()) {
                throw new ValidationException("Список категорий передан неверно неверно");
            }
        }
        List<Event> events = eventRepository
                .findByInitiatorInAndStateInAndCategoryInAndEventDateAfterAndEventDateBeforeOrderByIdAsc(
                        usersList, eventStates, categoriesList, start, end, pageRequest
                );
        if (events.isEmpty()) {
            return new ArrayList<>();
        }
        return eventMapper.toEventFullDtoList(events);
    }

    @Override
    public EventFullDto approveEventByAdmin(Long eventId, EventUpdateDto eventUpdateDto) {
        Event oldEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = {} не существует." + eventId));
        if ((eventUpdateDto.getEventDate() != null
                && eventUpdateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(1)))
                || oldEvent.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("Событие не может начинаться ранее, чем через 1 час после редактирования.");
        }
        if (oldEvent.getPublishedOn() != null && LocalDateTime.now().plusHours(1).isBefore(oldEvent.getPublishedOn())) {
            throw new ConflictException("Дата и время изменяемого события не соответствует требованиям.");
        }
        if (oldEvent.getState() == EventState.PUBLISHED || oldEvent.getState() == EventState.CANCELED) {
            throw new ConflictException("Редактирование статуса недоступно для опубликованного или отмененного события.");
        }
        if (eventUpdateDto.getCategory() != null) {
            Long catId = eventUpdateDto.getCategory();
            Category category = categoryRepository.findById(catId)
                    .orElseThrow(() -> new ValidationException("Категории с id = {} не существует." + catId));
            oldEvent.setCategory(category);
        }
        Optional.ofNullable(eventUpdateDto.getTitle()).ifPresent(oldEvent::setTitle);
        Optional.ofNullable(eventUpdateDto.getAnnotation()).ifPresent(oldEvent::setAnnotation);
        Optional.ofNullable(eventUpdateDto.getDescription()).ifPresent(oldEvent::setDescription);
        Optional.ofNullable(eventUpdateDto.getEventDate()).ifPresent(oldEvent::setEventDate);
        Optional.ofNullable(eventUpdateDto.getLocation()).ifPresent(oldEvent::setLocation);
        Optional.ofNullable(eventUpdateDto.getParticipantLimit()).ifPresent(oldEvent::setParticipantLimit);
        Optional.ofNullable(eventUpdateDto.getPaid()).ifPresent(oldEvent::setPaid);
        Optional.ofNullable(eventUpdateDto.getRequestModeration()).ifPresent(oldEvent::setRequestModeration);

        if (eventUpdateDto.getStateAction() != null
                && oldEvent.getState() == EventState.PENDING
                && eventUpdateDto.getStateAction() == StateAction.PUBLISH_EVENT) {
            oldEvent.setState(EventState.PUBLISHED);
            oldEvent.setPublishedOn(LocalDateTime.now());
        }
        if (eventUpdateDto.getStateAction() != null
                && oldEvent.getState() == EventState.PENDING
                && eventUpdateDto.getStateAction() == StateAction.REJECT_EVENT) {
            oldEvent.setState(EventState.CANCELED);
            oldEvent.setPublishedOn(null);
        }
        return eventMapper.toEventFullDto(oldEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllPublic(String text,
                                            List<Long> categories,
                                            Boolean paid,
                                            String rangeStart,
                                            String rangeEnd,
                                            boolean onlyAvailable,
                                            EventSort sort,
                                            int from,
                                            int size,
                                            HttpServletRequest request) {

        LocalDateTime start = (rangeStart != null)
                ? LocalDateTime.parse(rangeStart, FORMATTER)
                : LocalDateTime.now();
        LocalDateTime end = (rangeEnd != null)
                ? LocalDateTime.parse(rangeEnd, FORMATTER)
                : LocalDateTime.now().plusYears(DEFAULT_YEARS_ADDITION);

        PageRequest pageRequest = PageRequest.of(from / size, size);
        if (start.isAfter(end)) {
            throw new ValidationException("Временной промежуток задан неверно.");
        }
        StringBuilder queryStr = new StringBuilder("""
                SELECT e
                FROM Event e
                JOIN FETCH e.category c
                WHERE e.eventDate >= :start AND e.eventDate <= :end
                """);
        if (text != null && !text.isEmpty()) {
            queryStr.append(" AND (LOWER(e.annotation) LIKE LOWER(:text) OR LOWER(e.description) LIKE LOWER(:text))");
        }
        if (categories != null && !categories.isEmpty()) {
            queryStr.append(" AND e.category.id IN :categories");
        }
        if (paid != null) {
            queryStr.append(" AND e.paid = :paid");
        }
        queryStr.append(" AND e.participantLimit > e.confirmedRequests");

        TypedQuery<Event> query = entityManager.createQuery(queryStr.toString(), Event.class)
                .setParameter("start", start)
                .setParameter("end", end);

        if (text != null && !text.isEmpty()) {
            query.setParameter("text", "%" + text + "%");
        }
        if (categories != null && !categories.isEmpty()) {
            query.setParameter("categories", categories);
        }
        if (paid != null) {
            query.setParameter("paid", paid);
        }
        query.setFirstResult(pageRequest.getPageNumber() * pageRequest.getPageSize());
        query.setMaxResults(pageRequest.getPageSize());

        List<Event> events = query.getResultList();
        Map<Long, Long> eventAndViews = statsService.getView(events.stream()
                .map(Event::getId).toList(), false);

        events.forEach(e -> e.setViews(Math.toIntExact(eventAndViews.getOrDefault(e.getId(), 0L))));

        if (sort != null) {
            if (sort == EventSort.EVENT_DATE) {
                events.sort(Comparator.comparing(Event::getEventDate));
            } else if (sort == EventSort.VIEWS) {
                events.sort(Comparator.comparing(Event::getViews).reversed());
            }
        }
        if (events.stream().noneMatch(e -> e.getState() == EventState.PUBLISHED)) {
            throw new ValidationException("Нет опубликованных событий.");
        }
        List<Event> paginatedEvents = events.stream().skip(from).toList();
        statsService.createStats(request.getRequestURI(), request.getRemoteAddr());

        return paginatedEvents.stream()
                .map(eventMapper::toEventShortDto)
                .peek(dto -> {
                    Long viewCount = eventAndViews.get(dto.getId());
                    dto.setViews(viewCount != null ? viewCount.intValue() : 0);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByIdPublic(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("События с id = {} не существует." + eventId));
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("У события должен быть статус <ОПУБЛИКОВАННО>.");
        }
        Map<Long, Long> view = statsService.getView(List.of(event.getId()), true);
        EventFullDto eventResponseLongDto = eventMapper.toEventFullDto(event);
        eventResponseLongDto.setViews(Math.toIntExact(view.getOrDefault(event.getId(), 0L)));
        statsService.createStats(request.getRequestURI(), request.getRemoteAddr());
        return eventResponseLongDto;
    }

    private void processRejectedRequests(List<ParticipationRequest> requests, Map<String, List<ParticipationRequestDto>> requestMap) {
        if (requests.stream().anyMatch(r -> r.getStatus() == ParticipationRequestStatus.CONFIRMED)) {
            throw new ConflictException(
                    "Запрос на установление статуса <ОТМЕНЕНА>. Подтвержденные заявки нельзя отменить."
            );
        }
        requests.forEach(r -> r.setStatus(ParticipationRequestStatus.REJECTED));
        List<ParticipationRequestDto> rejectedRequests = requests.stream()
                .map(participationRequestMapper::toParticipationRequestDto)
                .toList();
        requestMap.put("rejectedRequests", rejectedRequests);
    }

    private void processConfirmedRequests(Event event, List<ParticipationRequest> requests, Map<String, List<ParticipationRequestDto>> requestMap) {
        if (requests.stream().anyMatch(r -> r.getStatus() != ParticipationRequestStatus.PENDING)) {
            throw new ConflictException(
                    "Запрос на установление статуса <ПОДТВЕРЖДЕНА>. Заявки должны быть со статусом <В ОЖИДАНИИ>."
            );
        }
        long limit = event.getParticipantLimit() - event.getConfirmedRequests();
        if (limit <= 0) {
            processRejectedRequests(requests, requestMap);
            return;
        }
        List<ParticipationRequest> confirmedList = requests.stream()
                .limit(limit)
                .peek(r -> r.setStatus(ParticipationRequestStatus.CONFIRMED))
                .toList();

        List<ParticipationRequestDto> confirmedRequests = confirmedList.stream()
                .map(participationRequestMapper::toParticipationRequestDto)
                .toList();
        requestMap.put("confirmedRequests", confirmedRequests);

        List<ParticipationRequest> rejectedList = requests.stream()
                .skip(limit)
                .peek(r -> r.setStatus(ParticipationRequestStatus.REJECTED))
                .toList();

        List<ParticipationRequestDto> rejectedRequests = rejectedList.stream()
                .map(participationRequestMapper::toParticipationRequestDto)
                .toList();
        requestMap.put("rejectedRequests", rejectedRequests);

        event.setConfirmedRequests(confirmedList.size() + event.getConfirmedRequests());
    }
}