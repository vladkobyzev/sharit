package ru.practicum.shareit.request.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class RequestRepositoryTest {
    @Autowired
    private RequestRepository requestRepository;

    @Test
    public void testFindAllByOwner_ShouldReturnAllItemsWithOwnerId() {
        long ownerId = 1L;
        ItemRequest itemRequest1 = new ItemRequest();
        itemRequest1.setId(1L);
        itemRequest1.setOwner(ownerId);
        requestRepository.save(itemRequest1);

        ItemRequest itemRequest2 = new ItemRequest();
        itemRequest2.setId(2L);
        itemRequest2.setOwner(ownerId);
        requestRepository.save(itemRequest2);

        ItemRequest itemRequest3 = new ItemRequest();
        itemRequest3.setId(3L);
        itemRequest3.setOwner(2L);
        requestRepository.save(itemRequest3);

        List<ItemRequest> itemRequests = requestRepository.findAllByOwner(ownerId);

        assertEquals(itemRequests.size(), 2);
    }

    @Test
    public void testFindAllByOwner_ShouldReturnEmptyListWhenNoItemsFound() {
        long ownerId = 1L;

        List<ItemRequest> itemRequests = requestRepository.findAllByOwner(ownerId);

        assertTrue(itemRequests.isEmpty());
    }

    @Test
    public void testFindAllByOwnerNot_ShouldReturnRequestsExceptForUser() {
        long ownerId = 1L;
        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setOwner(ownerId);
        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);
        request2.setOwner(ownerId + 1);
        ItemRequest request3 = new ItemRequest();
        request3.setId(3L);
        request3.setOwner(ownerId + 2);

        requestRepository.saveAll(Arrays.asList(request1, request2, request3));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").ascending());
        List<ItemRequest> requests = requestRepository.findAllByOwnerNot(ownerId, pageable);

        assertEquals(requests.size(), 2);
    }

    @Test
    public void testFindAllByOwnerNot_ShouldReturnEmptyListIfAllRequestsBelongToUser() {
        long ownerId = 1L;
        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setOwner(ownerId);
        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);
        request2.setOwner(ownerId);
        ItemRequest request3 = new ItemRequest();
        request3.setId(3L);
        request3.setOwner(ownerId);

        requestRepository.saveAll(Arrays.asList(request1, request2, request3));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").ascending());
        List<ItemRequest> requests = requestRepository.findAllByOwnerNot(ownerId, pageable);

        assertTrue(requests.isEmpty());
    }

    @Test
    public void testFindAllByOwnerNot_ShouldReturnEmptyListIfNoRequests() {
        long ownerId = 1L;

        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").ascending());
        List<ItemRequest> requests = requestRepository.findAllByOwnerNot(ownerId, pageable);

        assertTrue(requests.isEmpty());
    }
}