package ru.practicum.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Data
@AllArgsConstructor
public class CompilationDtoOutput {

    private Long id;

    private List<EventShortDto> events;

    private Boolean pinned;

    private String title;
}