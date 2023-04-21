package ru.practicum.shareit.request.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByOwner(Long ownerId);

    List<ItemRequest> findAllByOwnerNot(Long ownerId, Pageable pageable);
}
