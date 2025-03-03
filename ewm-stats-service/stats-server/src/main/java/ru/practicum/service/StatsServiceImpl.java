package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.DtoInput;
import ru.practicum.DtoOutput;
import ru.practicum.dao.StatsRepository;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public DtoInput createStat(DtoInput dtoInput) {
        Stat stat = statsRepository.save(StatsMapper.INSTANCE.toStats(dtoInput));
        return StatsMapper.INSTANCE.toStatsDtoInput(stat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoOutput> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала и дата окончания не могут быть равны друг другу.");
        }

        if (unique) {
            if (uris != null) {
                return statsRepository.findAllWithUniqueIpWithUris(uris, start, end);
            }
            return statsRepository.findAllWithUniqueIpWithoutUris(start, end);
        } else {
            if (uris != null) {
                return statsRepository.findAllWithUris(uris, start, end);
            }
            return statsRepository.findAllWithoutUris(start, end);
        }
    }
}