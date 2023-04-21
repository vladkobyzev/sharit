package ru.practicum.shareit.user.services;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();

    UserDto createUser(UserDto userDto);

    UserDto updateUser(UserDto userDto, long id);

    void deleteUser(long userId);

    UserDto getUserDtoById(long id);

    void isExistUser(long userId);

    User getUserById(long userId);
}
