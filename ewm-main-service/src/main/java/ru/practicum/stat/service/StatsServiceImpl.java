package ru.practicum.stat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.DtoInput;
import ru.practicum.DtoOutput;
import ru.practicum.stat.client.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.event.dto.EventFullDto.DATE_TIME_FORMAT;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    private final StatsClient statsClient;

    @Override
    public void createStats(final String uri, final String ip) {
        final DtoInput dtoInput = new DtoInput();
        dtoInput.setApp("ewm-main-service");
        dtoInput.setIp(ip);
        dtoInput.setUri(uri);
        dtoInput.setTimestamp(LocalDateTime.now());
        final DtoInput stat = statsClient.createStats(dtoInput);
    }

    @Override
    public List<DtoOutput> getStats(final List<Long> eventsId, final boolean unique) {
        final String start = LocalDateTime.now().minusYears(20).format(FORMATTER);
        final String end = LocalDateTime.now().plusYears(20).format(FORMATTER);
        final String[] uris = eventsId.stream()
                .map(id -> String.format("/events/%d", id))
                .toArray(String[]::new);
        return statsClient.getStats(start, end, uris, unique);
    }

    @Override
    public Map<Long, Long> getView(List<Long> eventsId, boolean unique) {
        final List<DtoOutput> stats = getStats(eventsId, unique);
        final Map<Long, Long> views = new HashMap<>();
        for (DtoOutput stat : stats) {
            final Long id = Long.valueOf(stat.getUri().replace("/events/", ""));
            final Long view = stat.getHits();
            views.put(id, view);
        }
        return views;
    }
}