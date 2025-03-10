package ru.practicum.stat.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.DtoInput;
import ru.practicum.DtoOutput;

import java.util.List;

@FeignClient(value = "stats-client", url = "http://stats-server:9090")
public interface StatsClient {

    @PostMapping("/hit")
    DtoInput createStats(@RequestBody DtoInput creationDto);

    @GetMapping("/stats")
    List<DtoOutput> getStats(@RequestParam String start,
                                 @RequestParam String end,
                                 @RequestParam(required = false) String[] uris,
                                 @RequestParam(defaultValue = "false") boolean unique);
}