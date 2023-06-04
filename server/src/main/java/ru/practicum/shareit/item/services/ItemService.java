package ru.practicum.shareit.item.services;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, long userId);

    Item getItemById(long itemId);

    List<ItemDto> getItems(long userId, Integer from, Integer size);

    void deleteItem(long id);

    ItemDto updateItem(ItemDto itemDto, long itemId, long userId);

    ItemDto getItemDtoById(long itemId, long userId);

    List<ItemDto> searchItemByText(String text);

    CommentDto createComment(CommentDto commentDto, long itemId, long userId);
}
