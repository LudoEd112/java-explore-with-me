package ru.practicum.service;

import ru.practicum.DtoInput;
import ru.practicum.DtoOutput;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    DtoInput createStat(DtoInput statDtoInput);

    List<DtoOutput> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}