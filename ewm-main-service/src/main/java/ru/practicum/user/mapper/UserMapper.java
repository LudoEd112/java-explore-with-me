package ru.practicum.user.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.user.dto.UserDtoInput;
import ru.practicum.user.dto.UserDtoOutput;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toUser(UserDtoInput userDtoInput) {
        return new User(
                null,
                userDtoInput.getName(),
                userDtoInput.getEmail()
        );
    }

    public UserDtoOutput toUserDto(User user) {
        return new UserDtoOutput(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public List<UserDtoOutput> toListDto(List<User> users) {
        return users.stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }
}