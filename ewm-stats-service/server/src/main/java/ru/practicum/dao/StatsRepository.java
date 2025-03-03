package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.DtoOutput;
import ru.practicum.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Stat, Long> {

    @Query("""
            SELECT new ru.practicum.StatsDtoOutput(s.ip, s.uri, COUNT(DISTINCT s.ip))
            FROM Stat AS s
            WHERE s.timestamp BETWEEN :start AND :end AND s.uri IN :uris
            GROUP BY s.ip, s.uri
            ORDER BY COUNT(DISTINCT s.ip) DESC
            """)
    List<DtoOutput> findAllWithUniqueIpWithUris(List<String> uris,
                                                LocalDateTime start,
                                                LocalDateTime end);

    @Query("""
            SELECT new ru.practicum.StatsDtoOutput(s.ip, s.uri, COUNT(DISTINCT s.ip))
            FROM Stat AS s
            WHERE s.timestamp BETWEEN :start AND :end
            GROUP BY s.ip, s.uri
            ORDER BY COUNT(DISTINCT s.ip) DESC
            """)
    List<DtoOutput> findAllWithUniqueIpWithoutUris(LocalDateTime start,
                                                   LocalDateTime end);

    @Query("""
            SELECT new ru.practicum.StatsDtoOutput(s.ip, s.uri, COUNT(s.ip))
            FROM Stat AS s
            WHERE s.timestamp BETWEEN :start AND :end AND s.uri IN :uris
            GROUP BY s.ip, s.uri
            ORDER BY COUNT (s.ip) DESC
            """)
    List<DtoOutput> findAllWithUris(List<String> uris,
                                    LocalDateTime start,
                                    LocalDateTime end);

    @Query("""
            SELECT new ru.practicum.StatsDtoOutput(s.ip, s.uri, COUNT(s.ip))
            FROM Stat AS s
            WHERE s.timestamp BETWEEN :start AND :end
            GROUP BY s.ip, s.uri
            ORDER BY COUNT (s.ip) DESC
            """)
    List<DtoOutput> findAllWithoutUris(LocalDateTime start,
                                       LocalDateTime end);

}