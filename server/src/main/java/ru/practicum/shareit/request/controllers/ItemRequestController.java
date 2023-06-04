package ru.practicum.shareit.request.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.services.ItemRequestService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/requests")
public class ItemRequestController {
    private static final String USER_ID = "X-Sharer-User-Id";
    private final ItemRequestService requestService;

    @Autowired
    public ItemRequestController(ItemRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ItemRequestDto createRequest(@RequestBody @Valid ItemRequestDto requestDto,
                                     @RequestHeader(value = USER_ID) long userId) {
        return requestService.createRequest(requestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnerRequests(@RequestHeader(value = USER_ID) long ownerId) {
        return requestService.getOwnerRequests(ownerId);

    }

    @GetMapping("/all")
    public List<ItemRequestDto> getUserRequests(@RequestHeader(value = USER_ID) long userId,
                                                @RequestParam(name = "from", required = false) Integer from,
                                                @RequestParam(name = "size", required = false) Integer size) {
        return requestService.getUserRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@PathVariable long requestId,
                                         @RequestHeader(value = USER_ID) long userId) {
        return requestService.getRequestById(requestId, userId);
    }
}
