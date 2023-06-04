package ru.practicum.shareit.user.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.AlreadyUsedEmail;
import ru.practicum.shareit.exceptions.EntityNotFound;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repositories.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceImplIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testCreateUser() {
        UserDto userDto = new UserDto();
        userDto.setName("John");
        userDto.setEmail("john@example.com");

        UserDto createdUserDto = userService.createUser(userDto);

        assertEquals(userDto.getName(), createdUserDto.getName());
        assertEquals(userDto.getEmail(), createdUserDto.getEmail());

        User createdUser = userRepository.findById(createdUserDto.getId()).orElse(null);

        assertNotNull(createdUser);
        assertEquals(userDto.getName(), createdUser.getName());
        assertEquals(userDto.getEmail(), createdUser.getEmail());
    }

    @Test
    public void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        String existingEmail = "test@example.com";
        UserDto existingUser = new UserDto();
        existingUser.setName("test1");
        existingUser.setEmail(existingEmail);
        userService.createUser(existingUser);

        UserDto userDto = new UserDto();
        userDto.setName("test2");
        userDto.setEmail(existingEmail);

        assertThrows(DataIntegrityViolationException.class, () -> userService.createUser(userDto));
    }

    @Test
    public void testGetAllUsers() {
        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail("user1@example.com");
        userRepository.save(user1);

        User user2 = new User();
        user2.setName("User 2");
        user2.setEmail("user2@example.com");
        userRepository.save(user2);

        List<UserDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("User 1", result.get(0).getName());
        assertEquals("user1@example.com", result.get(0).getEmail());
        assertEquals("User 2", result.get(1).getName());
        assertEquals("user2@example.com", result.get(1).getEmail());
    }

    @Test
    public void testGetAllUsersWithNoUsers() {
        List<UserDto> result = userService.getAllUsers();

        assertEquals(0, result.size());
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setName("test1");
        user.setEmail("test1@example.com");
        User savedUser = userRepository.save(user);

        UserDto userDto = new UserDto();
        userDto.setId(savedUser.getId());
        userDto.setName("test2");
        userDto.setEmail("test2@example.com");

        UserDto updatedUser = userService.updateUser(userDto, savedUser.getId());

        assertNotNull(updatedUser);
        assertEquals(userDto.getId(), updatedUser.getId());
        assertEquals(userDto.getName(), updatedUser.getName());
        assertEquals(userDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    public void testUpdateUserNonExistingUser() {
        UserDto userDto = new UserDto();
        userDto.setName("test1");
        userDto.setEmail("test1@example.com");

        assertThrows(EntityNotFound.class, () -> userService.updateUser(userDto, 1));
    }

    @Test
    public void testUpdateUserDuplicateEmail() {
        User user = new User();
        user.setName("test1");
        user.setEmail("test1@example.com");
        User savedUser = userRepository.save(user);

        User user2 = new User();
        user2.setName("test2");
        user2.setEmail("test2@example.com");
        userRepository.save(user2);

        UserDto userDto = new UserDto();
        userDto.setName("test1");
        userDto.setEmail(user2.getEmail());

        assertThrows(AlreadyUsedEmail.class, () -> userService.updateUser(userDto, savedUser.getId()));
    }

    @Test
    public void testDeleteUser() {
        User user = new User();
        user.setName("test1");
        user.setEmail("test1@example.com");
        User savedUser = userRepository.save(user);

        userService.deleteUser(savedUser.getId());
    }

    @Test
    public void testGetUserDtoById_Success() {
        User user = new User();
        user.setName("test1");
        user.setEmail("test1@example.com");
        User savedUser = userRepository.save(user);

        UserDto userFromDb = userService.getUserDtoById(savedUser.getId());
        assertEquals(userFromDb.getEmail(), user.getEmail());
    }

    @Test
    public void testGetUserDtoById_shouldTrowException() {
        User user = new User();
        user.setId(1L);
        user.setName("test1");
        user.setEmail("test1@example.com");

        assertThrows(EntityNotFound.class, () -> userService.getUserDtoById(user.getId()));
    }

    @Test
    public void testisExistUser_shouldTrowException() {
        User user = new User();
        user.setId(1L);
        user.setName("test1");
        user.setEmail("test1@example.com");

        assertThrows(EntityNotFound.class, () -> userService.isExistUser(user.getId()));
    }

    @Test
    public void testGetUserById_shouldTrowException() {
        User user = new User();
        user.setId(1L);
        user.setName("test1");
        user.setEmail("test1@example.com");

        assertThrows(EntityNotFound.class, () -> userService.getUserById(user.getId()));
    }

    @Test
    public void testGetUserById_Success() {
        User user = new User();
        user.setName("test1");
        user.setEmail("test1@example.com");
        User savedUser = userRepository.save(user);

        User userFromDb = userService.getUserById(savedUser.getId());
        assertEquals(userFromDb.getEmail(), user.getEmail());
    }
}
