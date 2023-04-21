package ru.practicum.shareit.item.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.services.UserService;
import ru.practicum.shareit.util.BookingStatus;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    public void updateItem_WithValidData_ShouldUpdateItemDescription() {
        long itemId = 1L;
        long userId = 2L;
        ItemDto itemDto = new ItemDto();
        itemDto.setDescription("new description");

        Item item = new Item();
        item.setId(itemId);
        item.setName("name");
        item.setDescription("old description");
        item.setOwner(userId);

        ItemDto itemDto1 = new ItemDto();
        itemDto1.setId(itemId);
        itemDto1.setName("name");
        itemDto1.setDescription("new description");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(modelMapper.map(item, ItemDto.class)).thenReturn(itemDto1);

        ItemDto updatedItem = itemService.updateItem(itemDto, itemId, userId);

        assertEquals(itemDto.getDescription(), updatedItem.getDescription());
        verify(itemRepository).save(item);
    }

    @Test
    public void updateItem_WithValidData_ShouldUpdateItemName() {
        long itemId = 1L;
        long userId = 2L;
        ItemDto itemDto = new ItemDto();
        itemDto.setName("new name");

        Item item = new Item();
        item.setId(itemId);
        item.setName("old name");
        item.setDescription("old description");
        item.setOwner(userId);

        ItemDto itemDto1 = new ItemDto();
        itemDto1.setId(itemId);
        itemDto1.setName("new name");
        item.setDescription("old description");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(modelMapper.map(item, ItemDto.class)).thenReturn(itemDto1);

        ItemDto updatedItem = itemService.updateItem(itemDto, itemId, userId);

        assertEquals(itemDto.getName(), updatedItem.getName());
        verify(itemRepository).save(item);
    }

    @Test
    public void updateItem_WithValidData_ShouldUpdateItemAvailable() {
        long itemId = 1L;
        long userId = 2L;
        ItemDto itemDto = new ItemDto();
        itemDto.setAvailable(true);

        Item item = new Item();
        item.setId(itemId);
        item.setName("old name");
        item.setDescription("old description");
        item.setOwner(userId);
        item.setAvailable(false);

        ItemDto itemDto1 = new ItemDto();
        itemDto1.setId(itemId);
        itemDto1.setName("old name");
        itemDto1.setDescription("old description");
        itemDto1.setAvailable(true);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(modelMapper.map(item, ItemDto.class)).thenReturn(itemDto1);

        ItemDto updatedItem = itemService.updateItem(itemDto, itemId, userId);

        assertEquals(itemDto.getAvailable(), updatedItem.getAvailable());
        verify(itemRepository).save(item);
    }

    @Test
    public void testUpdateItem_shouldThrowInappropriateUserException() {
        long itemId = 1L;
        long userId = 2L;
        ItemDto itemDto = new ItemDto();
        itemDto.setName("new name");
        itemDto.setDescription("new description");

        Item item = new Item();
        item.setId(itemId);
        item.setName("old name");
        item.setDescription("old description");
        item.setOwner(3L);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(InappropriateUser.class, () -> itemService.updateItem(itemDto, itemId, userId));
    }

    @Test
    public void testUpdateUser_withNonExistentUserId_shouldThrowException() {
        long itemId = 1L;
        long userId = 1L;
        ItemDto updatedItemDto = new ItemDto();
        updatedItemDto.setDescription("text");

        final EntityNotFound exception = assertThrows(EntityNotFound.class,
                () -> itemService.updateItem(updatedItemDto, itemId, userId));

        assertEquals("Item not found: " + itemId, exception.getMessage());
    }

    @Test
    public void testDeleteItem() {
        long userId = 1L;

        itemService.deleteItem(userId);

        verify(itemRepository).deleteById(userId);
    }

    @Test
    public void testCreateComment_success() {
        long itemId = 1L;
        long userId = 1L;
        CommentDto commentDto = new CommentDto();
        commentDto.setText("text");
        commentDto.setAuthorName("John");
        User user = new User();
        user.setName("Bob");
        Item item = new Item();

        Comment comment = new Comment();
        comment.setText("text");
        comment.setAuthorName("John");
        comment.setItem(item);

        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(bookingRepository.existsBookingByBooker_IdAndItem_IdAndStatusAndStartBefore(eq(userId), eq(itemId), eq(BookingStatus.APPROVED), any(LocalDateTime.class))).thenReturn(true);
        when(userService.getUserById(userId)).thenReturn(user);
        when(itemRepository.findById(userId)).thenReturn(Optional.of(item));

        itemService.createComment(commentDto, itemId, userId);

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    public void testCreateComment_withEmptyText_shouldThrowException() {
        long itemId = 1L;
        long userId = 1L;
        CommentDto commentDto = new CommentDto();
        commentDto.setText("");

        final BadRequest exception = assertThrows(BadRequest.class,
                () -> itemService.createComment(commentDto, itemId, userId));

        assertEquals("Empty comment text", exception.getMessage());
    }

    @Test
    public void testCreateComment_withoutBooking_shouldThrowException() {
        long itemId = 1L;
        long userId = 1L;
        CommentDto commentDto = new CommentDto();
        commentDto.setText("text");

        final BadRequest exception = assertThrows(BadRequest.class,
                () -> itemService.createComment(commentDto, itemId, userId));

        assertEquals("User " + userId + " doesnt use this item " + itemId, exception.getMessage());
    }

    @Test
    public void testCreateItem_success() {
        long userId = 1L;
        ItemDto itemDto = new ItemDto();
        itemDto.setName("new name");
        itemDto.setDescription("new description");
        itemDto.setAvailable(true);

        Item item = new Item();
        item.setName("new name");
        item.setDescription("new description");
        item.setAvailable(true);

        doNothing().when(userService).isExistUser(userId);
        when(modelMapper.map(itemDto, Item.class)).thenReturn(item);

        itemService.createItem(itemDto, userId);

        verify(itemRepository).save(any(Item.class));
    }

    @Test
    public void testCreateItem_NonExistentUserId_shouldThrowException() {
        long userId = 1L;
        ItemDto itemDto = new ItemDto();

        doThrow(new EntityNotFound("User not found: " + userId))
                .when(userService).isExistUser(userId);

        final EntityNotFound exception = assertThrows(EntityNotFound.class,
                () -> itemService.createItem(itemDto, userId));

        assertEquals("User not found: " + userId, exception.getMessage());
    }

    @Test
    public void testSearchItemByTextWhenTextIsNotBlank() {
        String text = "search text";
        Item item1 = new Item();
        Item item2 = new Item();
        List<Item> items = Arrays.asList(item1, item2);

        when(itemRepository.searchItemByText(text)).thenReturn(items);

        List<ItemDto> result = itemService.searchItemByText(text);

        assertEquals(items.size(), result.size());
        verify(itemRepository, times(1)).searchItemByText(text);
    }

    @Test
    public void testSearchItemByTextWhenTextIsNotBlankAndItemsNotExist_shouldReturnEmptyList() {
        String text = "search text";

        when(itemRepository.searchItemByText(text)).thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.searchItemByText(text);

        assertTrue(result.isEmpty());
        verify(itemRepository, times(1)).searchItemByText(text);
    }

    @Test
    public void testSearchItemByTextWhenTextIsBlank_shouldReturnEmptyList() {
        String text = "";

        List<ItemDto> result = itemService.searchItemByText(text);

        assertTrue(result.isEmpty());

        verifyNoInteractions(itemRepository);
    }

    @Test
    public void testGetItems_withPagination_success() {
        long ownerId = 1L;
        int from = 0;
        int size = 10;

        List<Item> items = new ArrayList<>();
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        items.add(item1);
        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Item 2");
        item2.setDescription("Description 2");
        items.add(item2);
        ItemDto itemDto1 = new ItemDto();
        itemDto1.setId(item1.getId());
        ItemDto itemDto2 = new ItemDto();
        itemDto2.setId(item2.getId());

        Page<Item> page = new PageImpl<>(items);

        when(itemRepository.findAllByOwner(ownerId, PageRequest.of(from, size))).thenReturn(page);
        when(modelMapper.map(item1, ItemDto.class)).thenReturn(itemDto1);
        when(modelMapper.map(item2, ItemDto.class)).thenReturn(itemDto2);

        List<ItemDto> itemsDto = itemService.getItems(ownerId, from, size);
        assertEquals(items.size(), itemsDto.size());
        assertEquals(item1.getId(), itemsDto.get(0).getId());
        assertEquals(item2.getId(), itemsDto.get(1).getId());
    }

    @Test
    public void testGetItems_withoutPagination_success() {
        long ownerId = 1L;
        Integer from = null;
        Integer size = null;

        List<Item> items = new ArrayList<>();
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        items.add(item1);
        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Item 2");
        item2.setDescription("Description 2");
        items.add(item2);
        ItemDto itemDto1 = new ItemDto();
        itemDto1.setId(item1.getId());
        ItemDto itemDto2 = new ItemDto();
        itemDto2.setId(item2.getId());

        when(itemRepository.findAllByOwner(ownerId)).thenReturn(items);
        when(modelMapper.map(item1, ItemDto.class)).thenReturn(itemDto1);
        when(modelMapper.map(item2, ItemDto.class)).thenReturn(itemDto2);

        List<ItemDto> itemsDto = itemService.getItems(ownerId, from, size);
        assertEquals(items.size(), itemsDto.size());
        assertEquals(item1.getId(), itemsDto.get(0).getId());
        assertEquals(item2.getId(), itemsDto.get(1).getId());
    }

    @Test
    public void testGetItems_withoutNextBooking_success() {
        long ownerId = 1L;
        Integer from = null;
        Integer size = null;

        List<Item> items = new ArrayList<>();
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        items.add(item1);
        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Item 2");
        item2.setDescription("Description 2");
        items.add(item2);
        ItemDto itemDto1 = new ItemDto();
        itemDto1.setId(item1.getId());
        ItemDto itemDto2 = new ItemDto();
        itemDto2.setId(item2.getId());

        when(itemRepository.findAllByOwner(ownerId)).thenReturn(items);
        when(bookingRepository.findAllNextBooking(eq(List.of(item1.getId(), item2.getId())), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(modelMapper.map(item1, ItemDto.class)).thenReturn(itemDto1);
        when(modelMapper.map(item2, ItemDto.class)).thenReturn(itemDto2);

        List<ItemDto> itemsDto = itemService.getItems(ownerId, from, size);
        assertEquals(items.size(), itemsDto.size());
        assertEquals(item1.getId(), itemsDto.get(0).getId());
        assertEquals(item2.getId(), itemsDto.get(1).getId());
        assertNull(itemsDto.get(0).getNextBooking());
        assertNull(itemsDto.get(1).getNextBooking());
    }

    @Test
    public void testGetItems_withoutLastBooking_success() {
        long ownerId = 1L;
        Integer from = null;
        Integer size = null;

        List<Item> items = new ArrayList<>();
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        items.add(item1);
        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Item 2");
        item2.setDescription("Description 2");
        items.add(item2);
        ItemDto itemDto1 = new ItemDto();
        itemDto1.setId(item1.getId());
        ItemDto itemDto2 = new ItemDto();
        itemDto2.setId(item2.getId());

        when(itemRepository.findAllByOwner(ownerId)).thenReturn(items);
        when(bookingRepository.findAllLastBooking(eq(List.of(item1.getId(), item2.getId())), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(modelMapper.map(item1, ItemDto.class)).thenReturn(itemDto1);
        when(modelMapper.map(item2, ItemDto.class)).thenReturn(itemDto2);

        List<ItemDto> itemsDto = itemService.getItems(ownerId, from, size);
        assertEquals(items.size(), itemsDto.size());
        assertEquals(item1.getId(), itemsDto.get(0).getId());
        assertEquals(item2.getId(), itemsDto.get(1).getId());
        assertNull(itemsDto.get(0).getLastBooking());
        assertNull(itemsDto.get(1).getLastBooking());
    }

    @Test
    public void testGetItems_withPagination_invalidPageRequest_shouldThrowBedRequest() {
        long ownerId = 1L;
        int from = 0;
        int size = 0;

        assertThrows(BadRequest.class, () -> itemService.getItems(ownerId, from, size));
    }

    @Test
    public void testGetItemDtoById_withBookingDate_success() {
        long itemId = 1L;
        long userId = 2L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastBooking = now.minusHours(1);
        LocalDateTime nextBooking = now.plusDays(1);

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(userId);

        User booker1 = new User();
        booker1.setId(1L);
        User booker2 = new User();
        booker2.setId(2L);

        Booking lastBookingDate1 = new Booking();
        lastBookingDate1.setId(1L);
        lastBookingDate1.setStart(lastBooking);
        lastBookingDate1.setBooker(booker1);

        Booking nextBookingDate1 = new Booking();
        nextBookingDate1.setId(2L);
        nextBookingDate1.setStart(nextBooking);
        nextBookingDate1.setBooker(booker2);

        BookingDateDto lastBookingDate = new BookingDateDto();
        lastBookingDate.setBookingDate(lastBooking);

        BookingDateDto nextBookingDate = new BookingDateDto();
        nextBookingDate.setBookingDate(nextBooking);

        ItemDto expectedDto = new ItemDto();
        expectedDto.setId(itemId);
        expectedDto.setLastBooking(lastBookingDate);
        expectedDto.setLastBooking(nextBookingDate);


        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(modelMapper.map(item, ItemDto.class)).thenReturn(expectedDto);
        when(bookingRepository.findLastBooking(eq(itemId), any(LocalDateTime.class))).thenReturn(lastBookingDate1);
        when(bookingRepository.findNextBooking(eq(itemId), any(LocalDateTime.class))).thenReturn(nextBookingDate1);



        ItemDto actualDto = itemService.getItemDtoById(itemId, userId);

        assertEquals(expectedDto.getId(), actualDto.getId());
        assertEquals(expectedDto.getLastBooking(), actualDto.getLastBooking());
        assertEquals(expectedDto.getNextBooking(), actualDto.getNextBooking());

        verify(itemRepository, times(1)).findById(itemId);
    }

    @Test
    public void testGetItemDtoById_withoutBookingDate_success() {
        long itemId = 1L;
        long ownerId = 2L;
        long userId = 3L;

        Item item = new Item();
        item.setId(itemId);
        item.setOwner(ownerId);
        item.setDescription("text");
        item.setAvailable(true);


        ItemDto expectedDto = new ItemDto();
        expectedDto.setId(itemId);
        expectedDto.setDescription("text");
        expectedDto.setAvailable(true);


        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(modelMapper.map(item, ItemDto.class)).thenReturn(expectedDto);

        ItemDto actualDto = itemService.getItemDtoById(itemId, userId);

        assertEquals(expectedDto.getId(), actualDto.getId());

        verifyNoInteractions(bookingRepository);
        verify(itemRepository, times(1)).findById(itemId);
    }

    @Test
    public void testGetItemDtoById_shouldThrowEntityNotFound() {
        long itemId = 1L;
        long userId = 1L;

        assertThrows(EntityNotFound.class, () -> itemService.getItemDtoById(itemId, userId));
    }

    @Test
    public void testGetItemById_shouldThrowEntityNotFound() {
        long itemId = 1L;

        assertThrows(EntityNotFound.class, () -> itemService.getItemById(itemId));
    }

    @Test
    public void testGetItemById_success() {
        long itemId = 1L;

        Item item = new Item();
        item.setId(itemId);
        item.setDescription("text");
        item.setAvailable(true);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        Item actual = itemService.getItemById(itemId);

        assertEquals(itemId, actual.getId());
        verify(itemRepository, times(1)).findById(itemId);
    }
}