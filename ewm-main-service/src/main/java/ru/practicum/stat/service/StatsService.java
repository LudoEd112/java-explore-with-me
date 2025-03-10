package ru.practicum.stat.service;

import ru.practicum.DtoOutput;

import java.util.List;
import java.util.Map;

public interface StatsService {

    void createStats(String uri, String ip);

    List<DtoOutput> getStats(List<Long> eventsId, boolean unique);

    Map<Long, Long> getView(List<Long> eventsId, boolean unique);
}