package ru.practicum.shareit.item.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositories.BookingRepository;
import ru.practicum.shareit.exceptions.BadRequest;
import ru.practicum.shareit.exceptions.EntityNotFound;
import ru.practicum.shareit.exceptions.InappropriateUser;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositories.CommentRepository;
import ru.practicum.shareit.item.repositories.ItemRepository;
import ru.practicum.shareit.user.services.UserService;
import ru.practicum.shareit.util.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ModelMapper mapper;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;

    @Autowired
    public ItemServiceImpl(ModelMapper mapper, ItemRepository itemRepository, BookingRepository bookingRepository, CommentRepository commentRepository, UserService userService) {
        this.mapper = mapper;
        this.itemRepository = itemRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItemDtoById(long itemId, long userId) {
        Item item = getItemById(itemId);
        item.getComments();
        ItemDto dto = convertItemToDto(item);
        if (item.getOwner() == userId) {
            dto.setLastBooking(convertBookingToDateDto(bookingRepository.findLastBooking(itemId, getCurrentTime())));
            dto.setNextBooking(convertBookingToDateDto(bookingRepository.findNextBooking(itemId, getCurrentTime())));
        }
        return dto;
    }

    @Override
    public Item getItemById(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFound("Item not found: " + itemId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getItems(long ownerId, Integer from, Integer size) {
        List<Item> items = getItemsPage(ownerId, from, size);
        List<ItemDto> itemsDto = items.stream()
                .peek(Item::getComments)
                .map(this::convertItemToDto)
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());

        setBookingDate(itemsDto);
        return itemsDto;
    }

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, long userId) {
        userService.isExistUser(userId);
        Item item = convertDtoToItem(itemDto);
        item.setOwner(userId);
        return convertItemToDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public CommentDto createComment(CommentDto commentDto, long itemId, long userId) {
        isValidComment(commentDto, itemId, userId);
        Comment comment = new Comment();
        setCommentField(comment, commentDto, itemId, userId);
        return convertCommentToDto(commentRepository.save(comment));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long itemId, long userId) {
        Item item = getItemById(itemId);
        if (item.getOwner() != userId) {
            throw new InappropriateUser("Item has a different owner" + userId);
        }
        setUpdateItemFields(item, itemDto);
        return convertItemToDto(itemRepository.save(item));
    }

    @Override
    public void deleteItem(long userId) {
        itemRepository.deleteById(userId);
    }

    @Override
    public List<ItemDto> searchItemByText(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.searchItemByText(text).stream()
                .map(this::convertItemToDto)
                .collect(Collectors.toList());
    }

    private void isValidComment(CommentDto commentDto, long itemId, long userId) {
        if (commentDto.getText().isBlank()) {
            throw new BadRequest("Empty comment text");
        }
        if (!bookingRepository.existsBookingByBooker_IdAndItem_IdAndStatusAndStartBefore(userId, itemId,
                BookingStatus.APPROVED, getCurrentTime())) {
            throw new BadRequest("User " + userId + " doesnt use this item " + itemId);
        }
    }

    private void setCommentField(Comment comment, CommentDto commentDto, long itemId, long userId) {
        comment.setText(commentDto.getText());
        comment.setAuthorName(userService.getUserById(userId).getName());
        comment.setItem(getItemById(itemId));
        comment.setCreated(getCurrentTime());
    }

    private void setUpdateItemFields(Item item, ItemDto itemDto) {
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
    }

    private Item convertDtoToItem(ItemDto itemDto) {
        return mapper.map(itemDto, Item.class);
    }

    private ItemDto convertItemToDto(Item item) {
        return mapper.map(item, ItemDto.class);
    }

    private CommentDto convertCommentToDto(Comment comment) {
        return mapper.map(comment, CommentDto.class);
    }

    private List<Item> getItemsPage(long ownerId, Integer from, Integer size) {
        if (from == null && size == null) {
            return itemRepository.findAllByOwner(ownerId);
        } else {
            if ((from == 0 && size == 0) || (from < 0 || size < 0)) {
                throw new BadRequest("Request without pagination");
            }
            return itemRepository.findAllByOwner(ownerId, PageRequest.of(from, size)).toList();
        }
    }

    private void setBookingDate(List<ItemDto> items) {
        List<Long> itemsId = items.stream()
                .map(ItemDto::getId).collect(Collectors.toList());

        List<Booking> allNextBooking = bookingRepository.findAllNextBooking(itemsId, getCurrentTime());
        if (!allNextBooking.isEmpty()) {
            for (int i = 0; i < allNextBooking.size(); i++) {
                items.get(i).setNextBooking(convertBookingToDateDto(allNextBooking.get(i)));
            }
        }
        List<Booking> allLastBooking = bookingRepository.findAllLastBooking(itemsId, getCurrentTime());
        if (!allLastBooking.isEmpty()) {
            for (int i = 0; i < allLastBooking.size(); i++) {
                items.get(i).setLastBooking(convertBookingToDateDto(allLastBooking.get(i)));
            }
        }
    }

    private BookingDateDto convertBookingToDateDto(Booking booking) {
        if (booking == null) return null;
        BookingDateDto dto = new BookingDateDto();
        dto.setId(booking.getId());
        dto.setBookingDate(booking.getStart());
        dto.setBookerId(booking.getBooker().getId());
        return dto;
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now();
    }
}
