package ru.practicum.shareit.item.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import ru.practicum.shareit.user.repositories.UserRepository;
import ru.practicum.shareit.util.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Transactional
public class ItemServiceImplIntegrationTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSearchItemByText_whenTextIsEmpty_shouldReturnEmptyList() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        User savedUser = userRepository.save(user);

        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        item1.setAvailable(true);
        item1.setOwner(savedUser.getId());
        itemRepository.save(item1);
        String text = "";

        List<ItemDto> result = itemService.searchItemByText(text);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void testSearchItemByText_whenMatchingItemsExist_shouldReturnList() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        User savedUser = userRepository.save(user);

        User user1 = new User();
        user1.setName("asd");
        user1.setEmail("asd@example.com");
        userRepository.save(user1);

        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        item1.setAvailable(true);
        item1.setOwner(savedUser.getId());
        itemRepository.save(item1);

        String text = "item";

        List<ItemDto> result = itemService.searchItemByText(text);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualToIgnoringCase("item 1");
    }

    @Test
    public void updateItem_withValidData_shouldUpdateItem() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        User savedUser = userRepository.save(user);

        Item item = new Item();
        item.setName("Item 1");
        item.setDescription("Description 1");
        item.setAvailable(true);
        item.setOwner(savedUser.getId());
        itemRepository.save(item);

        ItemDto itemDto = new ItemDto();
        itemDto.setDescription("update");

        ItemDto updatedItemDto = itemService.updateItem(itemDto, item.getId(), user.getId());


        assertEquals(itemDto.getDescription(), updatedItemDto.getDescription());
        assertEquals(updatedItemDto.getName(), item.getName());
    }

    @Test
    public void updateItem_withInvalidUserId_shouldThrowInappropriateUser() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        User savedUser = userRepository.save(user);

        Item item = new Item();
        item.setName("Item 1");
        item.setDescription("Description 1");
        item.setAvailable(true);
        item.setOwner(savedUser.getId());
        itemRepository.save(item);

        ItemDto itemDto = new ItemDto();
        itemDto.setDescription("update");

        assertThrows(InappropriateUser.class, () -> itemService.updateItem(itemDto, item.getId(), 1000));
    }

    @Test
    public void updateItem_WithInvalidUserId_shouldThrowEntityNotFound() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        User savedUser = userRepository.save(user);

        Item item = new Item();
        item.setName("Item 1");
        item.setDescription("Description 1");
        item.setAvailable(true);
        item.setOwner(savedUser.getId());
        itemRepository.save(item);

        ItemDto itemDto = new ItemDto();
        itemDto.setDescription("update");

        assertThrows(EntityNotFound.class, () -> itemService.updateItem(itemDto, 1000, savedUser.getId()));
    }

    @Test
    public void createComment_WithValidData_shouldCreateComment() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        userRepository.save(user);

        Item item = new Item();
        item.setName("Item 1");
        item.setDescription("Description 1");
        item.setAvailable(true);
        item.setOwner(user.getId());
        itemRepository.save(item);

        Booking b1 = new Booking();
        b1.setBooker(user);
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.APPROVED);
        b1.setItem(item);
        bookingRepository.save(b1);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Test comment");


        CommentDto createdComment = itemService.createComment(commentDto, item.getId(), user.getId());

        assertNotNull(createdComment);
        assertEquals(commentDto.getText(), createdComment.getText());
        assertEquals(user.getName(), createdComment.getAuthorName());
    }

    @Test
    public void createComment_WithInvalidUserId_shouldThrowBadRequestException() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        userRepository.save(user);

        Item item = new Item();
        item.setName("Item 1");
        item.setDescription("Description 1");
        item.setAvailable(true);
        item.setOwner(user.getId());
        itemRepository.save(item);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Test comment");

        assertThrows(BadRequest.class, () -> itemService.createComment(commentDto, item.getId(), Long.MAX_VALUE));
    }

    @Test
    public void createComment_WithEmptyText_shouldThrowBadRequestException() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        userRepository.save(user);

        Item item = new Item();
        item.setName("Item 1");
        item.setDescription("Description 1");
        item.setAvailable(true);
        item.setOwner(user.getId());
        itemRepository.save(item);

        Booking b1 = new Booking();
        b1.setBooker(user);
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.APPROVED);
        b1.setItem(item);
        bookingRepository.save(b1);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("");

        assertThrows(BadRequest.class, () -> itemService.createComment(commentDto, item.getId(), user.getId()));
    }

    @Test
    public void createItem_withValidData_shouldCreateItem() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        User savedUser = userRepository.save(user);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item 1");
        itemDto.setDescription("Description 1");
        itemDto.setAvailable(true);


        ItemDto savedItem = itemService.createItem(itemDto, savedUser.getId());


        assertNotNull(itemDto);
        assertEquals(itemDto.getName(), savedItem.getName());
    }

    @Test
    public void createItem_withValidData_shouldThrowEntityNotFound() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        userRepository.save(user);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Item 1");
        itemDto.setDescription("Description 1");
        itemDto.setAvailable(true);


        assertThrows(EntityNotFound.class, () -> itemService.createItem(itemDto, 1000));
    }

    @Test
    public void testGetItems_noPagination() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        User savedUser = userRepository.save(user);

        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        item1.setAvailable(true);
        item1.setOwner(savedUser.getId());
        Item savedItem1 = itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Item 2");
        item2.setDescription("Description 2");
        item2.setAvailable(false);
        item2.setOwner(savedUser.getId());
        Item savedItem2 = itemRepository.save(item2);

        Comment comment1 = new Comment();
        comment1.setAuthorName(savedUser.getName());
        comment1.setText("Comment 1");
        comment1.setItem(savedItem1);
        commentRepository.save(comment1);

        Comment comment2 = new Comment();
        comment2.setAuthorName(savedUser.getName());
        comment2.setText("Comment 2");
        comment2.setItem(savedItem1);
        commentRepository.save(comment2);


        List<ItemDto> items = itemService.getItems(savedUser.getId(), null, null);

        assertEquals(2, items.size());
        assertEquals(savedItem1.getId(), items.get(0).getId());
        assertEquals("Item 1", items.get(0).getName());
        assertEquals("Description 1", items.get(0).getDescription());
        assertTrue(items.get(0).getAvailable());

        assertEquals(savedItem2.getId(), items.get(1).getId());
        assertEquals("Item 2", items.get(1).getName());
        assertEquals("Description 2", items.get(1).getDescription());
        assertFalse(items.get(1).getAvailable());
    }

    @Test
    public void getItems_withPagination_shouldReturnItems() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        userRepository.save(user);

        Item item1 = new Item();
        item1.setName("Test Item 1");
        item1.setDescription("Test Item 1 Description");
        item1.setOwner(user.getId());
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Test Item 2");
        item2.setDescription("Test Item 2 Description");
        item2.setOwner(user.getId());
        itemRepository.save(item2);

        List<ItemDto> items = itemService.getItems(user.getId(), 0, 1);

        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("Test Item 1", items.get(0).getName());
    }

    @Test
    public void getItems_withInvalidPaginationRequest_shouldThrowBadRequest() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        userRepository.save(user);

        Item item1 = new Item();
        item1.setName("Test Item 1");
        item1.setDescription("Test Item 1 Description");
        item1.setOwner(user.getId());
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Test Item 2");
        item2.setDescription("Test Item 2 Description");
        item2.setOwner(user.getId());
        itemRepository.save(item2);

        assertThrows(BadRequest.class, () -> itemService.getItems(user.getId(), 0, 0));
    }

    @Test
    public void testGetItemDtoById_withValidItemIdAndUserId_shouldReturnItemDtoWithDate() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        userRepository.save(user);

        Item item = new Item();
        item.setName("Test item");
        item.setOwner(user.getId());
        item.setDescription("Test description");
        itemRepository.save(item);

        Booking lastBooking = new Booking();
        lastBooking.setBooker(user);
        lastBooking.setStart(now.minusMonths(2));
        lastBooking.setEnd(now.minusMonths(1));
        lastBooking.setStatus(BookingStatus.APPROVED);
        lastBooking.setItem(item);
        bookingRepository.save(lastBooking);

        Booking nextBooking = new Booking();
        nextBooking.setBooker(user);
        nextBooking.setStart(now.plusHours(1));
        nextBooking.setEnd(now.plusHours(3));
        nextBooking.setStatus(BookingStatus.APPROVED);
        nextBooking.setItem(item);
        bookingRepository.save(nextBooking);

        ItemDto itemDto = itemService.getItemDtoById(item.getId(), user.getId());

        assertNotNull(itemDto);
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(lastBooking.getStart(), itemDto.getLastBooking().getBookingDate());
        assertEquals(nextBooking.getStart(), itemDto.getNextBooking().getBookingDate());
    }

    @Test
    public void testGetItemDtoById_withValidItemIdAndUserId_shouldReturnItemDtoWithoutDate() {
        LocalDateTime now = LocalDateTime.now();
        User owner = new User();
        owner.setName("John");
        owner.setEmail("john@example.com");
        userRepository.save(owner);

        User user = new User();
        user.setName("Bob");
        user.setEmail("Bob@example.com");
        userRepository.save(user);

        Item item = new Item();
        item.setName("Test item");
        item.setOwner(owner.getId());
        item.setDescription("Test description");
        itemRepository.save(item);

        Booking lastBooking = new Booking();
        lastBooking.setBooker(owner);
        lastBooking.setStart(now.minusMonths(2));
        lastBooking.setEnd(now.minusMonths(1));
        lastBooking.setStatus(BookingStatus.APPROVED);
        lastBooking.setItem(item);
        bookingRepository.save(lastBooking);

        Booking nextBooking = new Booking();
        nextBooking.setBooker(owner);
        nextBooking.setStart(now.plusHours(1));
        nextBooking.setEnd(now.plusHours(3));
        nextBooking.setStatus(BookingStatus.APPROVED);
        nextBooking.setItem(item);
        bookingRepository.save(nextBooking);

        ItemDto itemDto = itemService.getItemDtoById(item.getId(), user.getId());

        assertNotNull(itemDto);
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertNull(itemDto.getNextBooking());
        assertNull(itemDto.getLastBooking());
    }

    @Test
    public void testGetItemDtoById_withInvalidItemId_shouldThrowEntityNotFound() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        userRepository.save(user);

        Item item = new Item();
        item.setId(1L);
        item.setName("Test item");
        item.setOwner(user.getId());
        item.setDescription("Test description");

        assertThrows(EntityNotFound.class, () -> itemService.getItemDtoById(item.getId(), user.getId()));
    }

    @Test
    public void testGetItemById_withInvalidItemId_shouldThrowEntityNotFound() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test item");
        item.setDescription("Test description");

        assertThrows(EntityNotFound.class, () -> itemService.getItemById(item.getId()));
    }

    @Test
    public void testDeleteItem() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        userRepository.save(user);

        Item item = new Item();
        item.setName("Test item");
        item.setOwner(user.getId());
        item.setDescription("Test description");
        itemRepository.save(item);

        itemService.deleteItem(item.getId());
    }
}
