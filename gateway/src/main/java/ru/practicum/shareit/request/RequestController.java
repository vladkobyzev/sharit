package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {
    private static final String USER_ID = "X-Sharer-User-Id";

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestBody @Valid ItemRequestDto requestDto,
                                        @RequestHeader(value = USER_ID) long userId) {
        return requestClient.createRequest(requestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerRequests(@RequestHeader(value = USER_ID) long ownerId) {
        return requestClient.getOwnerRequests(ownerId);

    }

    @GetMapping("/all")
    public ResponseEntity<Object> getUserRequests(@RequestHeader(value = USER_ID) long userId,
                                                @RequestParam(name = "from", required = false) Integer from,
                                                @RequestParam(name = "size", required = false) Integer size) {
        return requestClient.getUserRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable long requestId,
                                                 @RequestHeader(value = USER_ID) long userId) {
        return requestClient.getRequestById(requestId, userId);
    }
}
