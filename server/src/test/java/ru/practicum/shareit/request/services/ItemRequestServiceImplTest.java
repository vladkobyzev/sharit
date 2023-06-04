package ru.practicum.shareit.request.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.exceptions.BadRequest;
import ru.practicum.shareit.exceptions.EntityNotFound;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repositories.RequestRepository;
import ru.practicum.shareit.user.services.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
class ItemRequestServiceImplTest {
    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Mock
    private ModelMapper modelMapper;


    @Test
    public void testCreateRequestSuccess() {
        long userId = 1L;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(1L);
        requestDto.setDescription("Test Description");
        requestDto.setCreated(LocalDateTime.now());
        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setOwner(userId);
        request.setCreated(requestDto.getCreated());


        doNothing().when(userService).isExistUser(userId);
        when(requestRepository.save(request)).thenReturn(request);
        when(modelMapper.map(requestDto, ItemRequest.class)).thenReturn(request);
        when(modelMapper.map(request, ItemRequestDto.class)).thenReturn(requestDto);

        ItemRequestDto result = requestService.createRequest(requestDto, userId);

        assertEquals(requestDto.getDescription(), result.getDescription());
        assertNotNull(result.getId());
        assertNotNull(result.getCreated());
    }

    @Test
    public void testCreateRequestWithInvalidUserId() {
        long userId = 1000L;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Test Description");


        doThrow(new EntityNotFound("User not found")).when(userService).isExistUser(userId);

        assertThrows(EntityNotFound.class, () -> requestService.createRequest(requestDto, userId));
    }

    @Test
    public void testGetRequestById() {
        long userId = 1;
        long requestId = 1;
        ItemRequest request = new ItemRequest();
        request.setOwner(userId);
        request.setDescription("text");
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(requestId);
        requestDto.setDescription("text");

        doNothing().when(userService).isExistUser(userId);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(modelMapper.map(request, ItemRequestDto.class)).thenReturn(requestDto);

        ItemRequestDto actualResultDto = requestService.getRequestById(requestId, userId);

        assertNotNull(actualResultDto);
        assertEquals(requestId, actualResultDto.getId());
        verify(requestRepository).findById(requestId);
    }

    @Test
    public void testGetRequestByIdInvalidUser() {
        long userId = 1000;
        long requestId = 1;
        ItemRequest request = new ItemRequest();
        request.setOwner(userId);

        assertThrows(EntityNotFound.class, () -> requestService.getRequestById(requestId, userId));
    }

    @Test
    public void testGetRequestByIdInvalidRequest() {
        long userId = 1;
        long requestId = -1;

        assertThrows(EntityNotFound.class, () -> requestService.getRequestById(requestId, userId));
    }

    @Test
    public void testGetOwnerRequests() {
        long ownerId = 1;
        List<ItemRequest> requests = new ArrayList<>();
        requests.add(new ItemRequest());
        requests.add(new ItemRequest());

        doNothing().when(userService).isExistUser(ownerId);
        when(requestRepository.findAllByOwner(ownerId)).thenReturn(requests);

        List<ItemRequestDto> result = requestService.getOwnerRequests(ownerId);
        assertEquals(requests.size(), result.size());
    }

    @Test
    public void testGetOwnerRequests_shouldReturnEmptyList() {
        long ownerId = 1;

        doNothing().when(userService).isExistUser(ownerId);
        when(requestRepository.findAllByOwner(ownerId)).thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = requestService.getOwnerRequests(ownerId);
        assertTrue(result.isEmpty());
        verify(requestRepository).findAllByOwner(ownerId);
    }


    @Test
    public void testGetOwnerRequestsInvalidUser() {
        long ownerId = -1;
        when(requestService.getOwnerRequests(ownerId)).thenThrow(new EntityNotFound("Entity not found"));

        assertThrows(EntityNotFound.class, () -> requestService.getOwnerRequests(ownerId));
    }

    @Test
    public void testGetUserRequests_noPagination_success() {
        long userId = 1L;
        List<ItemRequest> requests = new ArrayList<>();
        ItemRequest itemRequest1 = new ItemRequest();
        itemRequest1.setId(1L);
        itemRequest1.setDescription("text");
        requests.add(itemRequest1);
        ItemRequest itemRequest2 = new ItemRequest();
        itemRequest1.setId(2L);
        itemRequest1.setDescription("text2");
        requests.add(itemRequest2);
        when(requestRepository.findAllByOwner(userId)).thenReturn(requests);

        List<ItemRequestDto> result = requestService.getUserRequests(userId, null, null);

        assertEquals(2, result.size());
        verify(requestRepository, times(1)).findAllByOwner(userId);
    }

    @Test
    public void testGetUserRequests_withPagination_success() {
        long userId = 1L;
        List<ItemRequest> requests = new ArrayList<>();
        ItemRequest itemRequest1 = new ItemRequest();
        itemRequest1.setId(1L);
        itemRequest1.setDescription("text");
        requests.add(itemRequest1);
        ItemRequest itemRequest2 = new ItemRequest();
        itemRequest1.setId(2L);
        itemRequest1.setDescription("text2");
        requests.add(itemRequest2);

        Page<ItemRequest> requestPage = new PageImpl<>(requests);
        when(requestRepository.findAllByOwnerNot(userId,
                PageRequest.of(1, 1, Sort.by("created")
                        .ascending()))).thenReturn(requestPage.toList());

        List<ItemRequestDto> result = requestService.getUserRequests(userId, 1, 1);

        assertEquals(2, result.size());
        verify(requestRepository, times(1)).findAllByOwnerNot(userId, PageRequest.of(1, 1, Sort.by("created").ascending()));
    }

    @Test
    public void testGetUserRequests_noPagination_shouldReturnEmptyList() {
        long userId = 1L;

        when(requestRepository.findAllByOwner(userId)).thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = requestService.getUserRequests(userId, null, null);

        assertTrue(result.isEmpty());
        verify(requestRepository, times(1)).findAllByOwner(userId);
    }

    @Test
    public void testGetUserRequests_withPagination_shouldReturnEmptyList() {
        long userId = 1L;

        when(requestRepository.findAllByOwnerNot(userId, PageRequest.of(1, 1, Sort.by("created").ascending()))).thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = requestService.getUserRequests(userId, 1, 1);

        assertTrue(result.isEmpty());
        verify(requestRepository, times(1)).findAllByOwnerNot(userId, PageRequest.of(1, 1, Sort.by("created").ascending()));
    }

    @Test
    public void testGetUserRequests_shouldThrowBedRequest() {
        long userId = 1L;
        int from = 0;
        int size = 0;

        assertThrows(BadRequest.class, () -> requestService.getUserRequests(userId, from, size));
    }
}