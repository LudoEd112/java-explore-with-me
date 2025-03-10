package ru.practicum.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParticipationRequestMapper {

    public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getRequester().getId(),
                request.getEvent().getId(),
                request.getStatus(),
                request.getCreated()
        );
    }

    public List<ParticipationRequestDto> toParticipationRequestDtoList(List<ParticipationRequest> requests) {
        return requests.stream()
                .map(this::toParticipationRequestDto)
                .collect(Collectors.toList());
    }
}