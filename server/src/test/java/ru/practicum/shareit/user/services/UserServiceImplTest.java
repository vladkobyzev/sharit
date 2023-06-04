package ru.practicum.shareit.user.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.exceptions.AlreadyUsedEmail;
import ru.practicum.shareit.exceptions.EntityNotFound;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repositories.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserRepository userRepository;

    @Test
    public void testUpdateUser_withValidUserIdAndNewName_shouldUpdateName() {
        long userId = 1L;
        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setName("John");

        User user = new User();
        user.setId(userId);
        user.setName("Bob");
        user.setEmail("john@example.com");

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setName("John");
        userDto.setEmail("john@example.com");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName(userDto.getName());
        updatedUser.setEmail(userDto.getEmail());


        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.findByEmail(updatedUserDto.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(modelMapper.map(updatedUser, UserDto.class)).thenReturn(userDto);


        UserDto actualUser = userService.updateUser(updatedUserDto, userId);

        assertNotNull(actualUser);
        assertEquals(actualUser.getName(), "John");
        assertEquals(actualUser.getEmail(), "john@example.com");
    }

    @Test
    public void testUpdateUser_withValidUserIdAndNewEmail_shouldUpdateEmail() {
        long userId = 1L;
        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setEmail("updated@example.com");

        User user = new User();
        user.setId(userId);
        user.setName("John");
        user.setEmail("old@Gmail.com");

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setName("John");
        userDto.setEmail("updated@example.com");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName(userDto.getName());
        updatedUser.setEmail(userDto.getEmail());


        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.findByEmail(updatedUserDto.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(modelMapper.map(updatedUser, UserDto.class)).thenReturn(userDto);


        UserDto actualUser = userService.updateUser(updatedUserDto, userId);

        assertNotNull(actualUser);
        assertEquals(actualUser.getName(), "John");
        assertEquals(actualUser.getEmail(), "updated@example.com");
    }

    @Test
    public void testUpdateUser_withNonExistentUserId_shouldThrowException() {
        long userId = 1L;
        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setEmail("update@example.com");

        final EntityNotFound exception = assertThrows(EntityNotFound.class,
                () -> userService.updateUser(updatedUserDto, userId));

        assertEquals("User not found: " + userId, exception.getMessage());
    }

    @Test
    public void testUpdateUser_withUsedEmail_shouldThrowException() {
        long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("John");
        existingUser.setEmail("john@example.com");

        User existingUser1 = new User();
        existingUser1.setId(2L);
        existingUser1.setName("Jane");
        existingUser1.setEmail("jane@example.com");

        UserDto updatUserDto = new UserDto();
        updatUserDto.setName("John Update");
        updatUserDto.setEmail("jane@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(updatUserDto.getEmail())).thenReturn(Optional.of(existingUser1));


        final AlreadyUsedEmail exception = assertThrows(AlreadyUsedEmail.class,
                () -> userService.updateUser(updatUserDto, userId));

        assertEquals("Already Used Email: " + "jane@example.com", exception.getMessage());
    }

    @Test
    public void testDeleteUser() {
        long userId = 1L;

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    public void testCreateUser_success() {
        long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("John");
        existingUser.setEmail("john@example.com");

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setName("John");
        userDto.setEmail("john@example.com");

        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(modelMapper.map(userDto, User.class)).thenReturn(existingUser);
        when(modelMapper.map(existingUser, UserDto.class)).thenReturn(userDto);

        userService.createUser(userDto);

        verify(userRepository).save(existingUser);
    }

    @Test
    public void testGetUserDtoById_success() {
        long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("John");
        existingUser.setEmail("john@example.com");

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setName("John");
        userDto.setEmail("john@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(modelMapper.map(existingUser, UserDto.class)).thenReturn(userDto);

        userService.getUserDtoById(userId);

        verify(userRepository).findById(userId);
    }

    @Test
    public void testGetUserDtoById_shouldThrowException() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenThrow(new EntityNotFound("User not found"));

        assertThrows(EntityNotFound.class, () -> userService.getUserDtoById(userId));
    }

    @Test
    public void testGetAllUsers_success() {
        User user = new User();
        User user1 = new User();
        User user2 = new User();
        List<User> users = List.of(user, user1, user2);

        when(userRepository.findAll()).thenReturn(users);
        List<UserDto> usersDto = userService.getAllUsers();
        assertEquals(3, usersDto.size());
    }

    @Test
    public void testGetAllUsersReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        List<UserDto> users = userService.getAllUsers();
        assertEquals(0, users.size());
    }

    @Test
    public void testIsExistUser_success() {
        when(userRepository.existsById(anyLong())).thenReturn(true);

        userService.isExistUser(1L);

        verify(userRepository).existsById(1L);
    }

    @Test
    public void testIsExistUser_shouldTrowEntityNotFound() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(EntityNotFound.class, () -> userService.isExistUser(1L));
    }

    @Test
    public void testGetUserById_shouldThrowEntityNotFound() {
        long itemId = 1L;

        assertThrows(EntityNotFound.class, () -> userService.getUserById(itemId));
    }

    @Test
    public void testGetUserById_success() {
        long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("John");
        user.setEmail("john@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User actual = userService.getUserById(userId);

        assertEquals(userId, actual.getId());
        verify(userRepository, times(1)).findById(userId);
    }
}