package ru.practicum.event.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.practicum.event.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {

}