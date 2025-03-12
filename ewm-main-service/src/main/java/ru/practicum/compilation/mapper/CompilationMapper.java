package ru.practicum.compilation.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.compilation.dto.CompilationDtoInput;
import ru.practicum.compilation.dto.CompilationDtoOutput;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;

import java.util.List;

@Component
public class CompilationMapper {

    public Compilation toCompilation(CompilationDtoInput dto, List<Event> events) {
        return new Compilation(
                null,
                events,
                dto.getPinned(),
                dto.getTitle()
        );
    }

    public CompilationDtoOutput toCompilationDto(Compilation compilation, List<EventShortDto> eventShortDtoList) {
        return new CompilationDtoOutput(
                compilation.getId(),
                eventShortDtoList,
                compilation.getPinned(),
                compilation.getTitle()
        );
    }
}