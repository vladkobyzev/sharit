package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private static final String USER_ID = "X-Sharer-User-Id";

    private final ItemClient itemClient;

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable long itemId,
                                              @RequestHeader(value = USER_ID) long userId) {
        return itemClient.getItemDtoById(itemId, userId);
    }

    @GetMapping()
    public ResponseEntity<Object> getItems(@RequestHeader(value = USER_ID) long userId,
                                  @RequestParam(name = "from", required = false) Integer from,
                                  @RequestParam(name = "size", required = false) Integer size) {
        return itemClient.getItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemsByText(@RequestParam String text) {
        return itemClient.searchItemByText(text);
    }


    @PostMapping()
    public ResponseEntity<Object> createItem(@RequestHeader(value = USER_ID) long userId,
                              @Valid @RequestBody ItemDto itemDto) {
        return itemClient.createItem(itemDto, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestBody CommentDto commentDto,
                                    @PathVariable long itemId,
                                    @RequestHeader(value = USER_ID) long userId) {
        return itemClient.createComment(commentDto, itemId, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable long itemId,
                              @RequestBody ItemDto itemDto,
                              @RequestHeader(value = USER_ID) long userId) {
        return itemClient.updateItem(itemDto, itemId, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteItem(@PathVariable long id) {
        return itemClient.deleteItem(id);
    }
}
