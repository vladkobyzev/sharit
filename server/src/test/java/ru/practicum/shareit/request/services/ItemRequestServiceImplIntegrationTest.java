package ru.practicum.shareit.request.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.BadRequest;
import ru.practicum.shareit.exceptions.EntityNotFound;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repositories.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemRequestServiceImplIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository itemRequestRepository;

    @Test
    public void shouldCreateRequest_success() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        userRepository.save(user);

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Test request");
        requestDto.setItems(Collections.emptyList());

        ItemRequestDto createdRequestDto = itemRequestService.createRequest(requestDto, user.getId());

        assertNotNull(createdRequestDto.getId());
        assertEquals(requestDto.getDescription(), createdRequestDto.getDescription());
        assertEquals(requestDto.getItems().size(), createdRequestDto.getItems().size());
        assertNotNull(createdRequestDto.getCreated());

        ItemRequest createdRequest = itemRequestRepository.findById(createdRequestDto.getId()).orElseThrow();
        assertEquals(user.getId(), createdRequest.getOwner());
        assertEquals(requestDto.getDescription(), createdRequest.getDescription());
        assertEquals(requestDto.getItems().size(), createdRequest.getItems().size());
        assertNotNull(createdRequest.getCreated());
    }

    @Test
    public void shouldCreateRequest_withInvalidUserId_shouldThrowEntityNotFound() {
        User invalidUser = new User();
        invalidUser.setId(1L);

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Test request");
        requestDto.setItems(Collections.emptyList());

        assertThrows(EntityNotFound.class, () -> itemRequestService.createRequest(requestDto, invalidUser.getId()));
    }

    @Test
    public void getRequestById_validId_shouldReturnsRequest() {
        User user = new User();
        user.setName("John");
        user.setEmail("johndoe@example.com");
        userRepository.save(user);

        ItemRequest request = new ItemRequest();
        request.setOwner(user.getId());
        request.setDescription("Test request");
        request.setItems(Collections.emptyList());
        request.setCreated(LocalDateTime.now());
        itemRequestRepository.save(request);

        ItemRequestDto result = itemRequestService.getRequestById(request.getId(), user.getId());

        assertEquals(request.getId(), result.getId());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getCreated(), result.getCreated());
    }

    @Test
    public void getRequestById_InvalidRequestId_ThrowsEntityNotFound() {
        long invalidId = 100L;
        User user = new User();
        user.setName("John");
        user.setEmail("johndoe@example.com");
        userRepository.save(user);

        assertThrows(EntityNotFound.class, () -> itemRequestService.getRequestById(invalidId, user.getId()));
    }

    @Test
    public void getRequestById_InvalidUserId_ThrowsEntityNotFound() {
        long requestId = 1L;
        long invalidUserId = 100L;
        User user = new User();
        user.setName("John");
        user.setEmail("johndoe@example.com");
        userRepository.save(user);

        ItemRequest request = new ItemRequest();
        request.setOwner(user.getId());
        request.setDescription("Test request");
        request.setCreated(LocalDateTime.now());
        itemRequestRepository.save(request);

        assertThrows(EntityNotFound.class, () -> itemRequestService.getRequestById(requestId, invalidUserId));
    }

    @Test
    public void testGetOwnerRequests() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        userRepository.save(user);

        ItemRequest request1 = new ItemRequest();
        request1.setDescription("Request 1");
        request1.setOwner(user.getId());
        itemRequestRepository.save(request1);

        ItemRequest request2 = new ItemRequest();
        request2.setDescription("Request 2");
        request2.setOwner(user.getId());
        itemRequestRepository.save(request2);

        ItemRequest request3 = new ItemRequest();
        request3.setDescription("Request 3");
        request3.setOwner(user.getId());
        itemRequestRepository.save(request3);

        List<ItemRequestDto> requests = itemRequestService.getOwnerRequests(user.getId());

        assertThat(requests).isNotNull();
        assertThat(requests.size()).isEqualTo(3);

        assertThat(requests.get(0).getDescription()).isEqualTo(request1.getDescription());

        assertThat(requests.get(1).getDescription()).isEqualTo(request2.getDescription());

        assertThat(requests.get(2).getDescription()).isEqualTo(request3.getDescription());
    }

    @Test
    public void getOwnerRequests_withInvalidOwnerId_shouldThrowEntityNotFound() {
        long ownerId = 1L;
        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setOwner(ownerId);
        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);
        request2.setOwner(ownerId);
        itemRequestRepository.save(request1);
        itemRequestRepository.save(request2);

        assertThrows(EntityNotFound.class, () -> itemRequestService.getOwnerRequests(999L));
    }

    @Test
    public void testGetUserRequests_noPagination() {
        User user1 = new User();
        user1.setName("Test User");
        user1.setEmail("test@example.com");
        userRepository.save(user1);
        User user2 = new User();
        user2.setName("Test User2");
        user2.setEmail("test2@example.com");
        userRepository.save(user2);

        ItemRequest request1 = new ItemRequest();
        request1.setDescription("Request 1");
        request1.setOwner(user1.getId());
        itemRequestRepository.save(request1);
        ItemRequest request2 = new ItemRequest();
        request2.setDescription("Request 2");
        request2.setOwner(user1.getId());
        itemRequestRepository.save(request2);
        ItemRequest request3 = new ItemRequest();
        request3.setDescription("Request 3");
        request3.setOwner(user2.getId());
        itemRequestRepository.save(request3);

        List<ItemRequestDto> requests = itemRequestService.getUserRequests(user1.getId(), null, null);

        assertThat(requests.size()).isEqualTo(2);
    }

    @Test
    public void testGetUserRequestsWithPagination() {
        User user1 = new User();
        user1.setName("Test User");
        user1.setEmail("test@example.com");
        userRepository.save(user1);
        User user2 = new User();
        user2.setName("Test User2");
        user2.setEmail("test2@example.com");
        userRepository.save(user2);
        ItemRequest request1 = new ItemRequest();
        request1.setDescription("Request 1");
        request1.setOwner(user1.getId());
        itemRequestRepository.save(request1);
        ItemRequest request2 = new ItemRequest();
        request2.setDescription("Request 2");
        request2.setOwner(user1.getId());
        itemRequestRepository.save(request2);
        ItemRequest request3 = new ItemRequest();
        request3.setDescription("Request 3");
        request3.setOwner(user2.getId());
        itemRequestRepository.save(request3);

        List<ItemRequestDto> requests = itemRequestService.getUserRequests(user1.getId(), 0, 1);

        assertThat(requests.size()).isEqualTo(1);
    }

    @Test
    public void testGetUserRequests_withInvalidRequestPagination_shouldThrowBadRequest() {
        User user1 = new User();
        user1.setName("Test User");
        user1.setEmail("test@example.com");
        userRepository.save(user1);
        User user2 = new User();
        user2.setName("Test User2");
        user2.setEmail("test2@example.com");
        userRepository.save(user2);
        ItemRequest request1 = new ItemRequest();
        request1.setDescription("Request 1");
        request1.setOwner(user1.getId());
        itemRequestRepository.save(request1);
        ItemRequest request2 = new ItemRequest();
        request2.setDescription("Request 2");
        request2.setOwner(user1.getId());
        itemRequestRepository.save(request2);
        ItemRequest request3 = new ItemRequest();
        request3.setDescription("Request 3");
        request3.setOwner(user2.getId());
        itemRequestRepository.save(request3);

        assertThrows(BadRequest.class, () -> itemRequestService.getUserRequests(user1.getId(), 0, 0));
    }
}