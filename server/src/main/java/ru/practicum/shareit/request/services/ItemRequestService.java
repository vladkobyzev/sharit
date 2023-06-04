package ru.practicum.shareit.request.services;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(ItemRequestDto requestDto, long userId);

    ItemRequestDto getRequestById(long requestId, long userId);

    List<ItemRequestDto> getOwnerRequests(long ownerId);

    List<ItemRequestDto> getUserRequests(long userId, Integer from, Integer size);
}
