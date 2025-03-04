package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.DtoInput;
import ru.practicum.model.Stat;

@Mapper
public interface StatsMapper {

    StatsMapper INSTANCE = Mappers.getMapper(StatsMapper.class);

    Stat toStats(DtoInput statDtoInput);

    DtoInput toStatsDtoInput(Stat stat);

}