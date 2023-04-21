package ru.practicum.shareit.user.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    public void findByEmail_shouldReturnOptional() {
        User user = new User();
        user.setName("test1");
        user.setEmail("test1@example.com");
        userRepository.save(user);
        Optional<User> savedUser = userRepository.findByEmail(user.getEmail());

        assertTrue(savedUser.isPresent());
        assertEquals(savedUser.get().getEmail(), user.getEmail());
    }

    @Test
    public void findByEmail_shouldReturnEmpty() {
        Optional<User> savedUser = userRepository.findByEmail("test1@example.com");

        assertTrue(savedUser.isEmpty());
    }
}