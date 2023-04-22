package ru.practicum.shareit.booking.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositories.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repositories.UserRepository;
import ru.practicum.shareit.util.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void testFindAllUserBookingsByState() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user = userRepository.save(user);

        LocalDateTime now = LocalDateTime.now();
        Booking b1 = new Booking();
        b1.setBooker(user);
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(b1);

        Booking b2 = new Booking();
        b2.setBooker(user);
        b2.setStart(now.minusHours(1));
        b2.setEnd(now.plusHours(1));
        b2.setStatus(BookingStatus.WAITING);
        bookingRepository.save(b2);

        Booking b3 = new Booking();
        b3.setBooker(user);
        b3.setStart(now.plusHours(1));
        b3.setEnd(now.plusHours(2));
        b3.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(b3);

        List<Booking> bookings = bookingRepository.findAllUserBookingsByState(user.getId(), "CURRENT", now);
        assertTrue(bookings.isEmpty());

        bookings = bookingRepository.findAllUserBookingsByState(user.getId(), "PAST", now);
        assertTrue(bookings.contains(b1));

        bookings = bookingRepository.findAllUserBookingsByState(user.getId(), "FUTURE", now);
        assertTrue(bookings.contains(b3));

        bookings = bookingRepository.findAllUserBookingsByState(user.getId(), "WAITING", now);
        assertTrue(bookings.contains(b2));

        bookings = bookingRepository.findAllUserBookingsByState(user.getId(), "REJECTED", now);
        assertTrue(bookings.contains(b1));

        bookings = bookingRepository.findAllUserBookingsByState(user.getId(), "ALL", now);
        assertTrue(bookings.contains(b1));
        assertTrue(bookings.contains(b2));
        assertTrue(bookings.contains(b3));
    }

    @Test
    public void testFindAllOwnerBookingsByState() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user = userRepository.save(user);

        User owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        Item item = new Item();
        item.setOwner(owner.getId());
        item.setAvailable(true);
        itemRepository.save(item);

        LocalDateTime now = LocalDateTime.now();
        Booking b1 = new Booking();
        b1.setBooker(user);
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.REJECTED);
        b1.setItem(item);
        bookingRepository.save(b1);

        Booking b2 = new Booking();
        b2.setBooker(user);
        b2.setStart(now.minusHours(1));
        b2.setEnd(now.plusHours(1));
        b2.setStatus(BookingStatus.WAITING);
        b2.setItem(item);
        bookingRepository.save(b2);

        Booking b3 = new Booking();
        b3.setBooker(user);
        b3.setStart(now.plusHours(1));
        b3.setEnd(now.plusHours(2));
        b3.setStatus(BookingStatus.APPROVED);
        b3.setItem(item);
        bookingRepository.save(b3);

        List<Booking> bookings = bookingRepository.findAllOwnerBookingsByState(owner.getId(), "PAST", now);
        assertTrue(bookings.contains(b1));

        bookings = bookingRepository.findAllOwnerBookingsByState(owner.getId(), "FUTURE", now);
        assertTrue(bookings.contains(b3));

        bookings = bookingRepository.findAllOwnerBookingsByState(owner.getId(), "WAITING", now);
        assertTrue(bookings.contains(b2));

        bookings = bookingRepository.findAllOwnerBookingsByState(owner.getId(), "REJECTED", now);
        assertTrue(bookings.contains(b1));

        bookings = bookingRepository.findAllOwnerBookingsByState(owner.getId(), "ALL", now);
        assertTrue(bookings.contains(b1));
        assertTrue(bookings.contains(b2));
        assertTrue(bookings.contains(b3));
    }

    @Test
    public void testFindAllUserBookingsByStateWithPagination() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user = userRepository.save(user);

        LocalDateTime now = LocalDateTime.now();
        Booking b1 = new Booking();
        b1.setBooker(user);
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(b1);

        Booking b2 = new Booking();
        b2.setBooker(user);
        b2.setStart(now.minusHours(1));
        b2.setEnd(now.plusHours(1));
        b2.setStatus(BookingStatus.WAITING);
        bookingRepository.save(b2);

        Booking b3 = new Booking();
        b3.setBooker(user);
        b3.setStart(now.plusHours(1));
        b3.setEnd(now.plusHours(2));
        b3.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(b3);


        int pageSize = 2;
        int pageNumber = 0;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("start").ascending());

        Slice<Booking> result = bookingRepository.findAllUserBookingsByState(user.getId(), "ALL", now, pageable);

        assertNotNull(result);
        assertTrue(result.hasContent());
        assertEquals(pageSize, result.getNumberOfElements());
        assertEquals(2, result.getContent().size());
        assertEquals(pageable, result.getPageable());
    }

    @Test
    public void testFindAllOwnerBookingsByStateWithPagination() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user = userRepository.save(user);

        User owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        Item item = new Item();
        item.setOwner(owner.getId());
        item.setAvailable(true);
        itemRepository.save(item);

        LocalDateTime now = LocalDateTime.now();
        Booking b1 = new Booking();
        b1.setBooker(user);
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.REJECTED);
        b1.setItem(item);
        bookingRepository.save(b1);

        Booking b2 = new Booking();
        b2.setBooker(user);
        b2.setStart(now.minusHours(1));
        b2.setEnd(now.plusHours(1));
        b2.setStatus(BookingStatus.WAITING);
        b2.setItem(item);
        bookingRepository.save(b2);

        Booking b3 = new Booking();
        b3.setBooker(user);
        b3.setStart(now.plusHours(1));
        b3.setEnd(now.plusHours(2));
        b3.setStatus(BookingStatus.APPROVED);
        b3.setItem(item);
        bookingRepository.save(b3);


        int pageSize = 2;
        int pageNumber = 0;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("start").ascending());

        Slice<Booking> result = bookingRepository.findAllOwnerBookingsByState(owner.getId(), "ALL", now, pageable);

        assertNotNull(result);
        assertTrue(result.hasContent());
        assertEquals(pageSize, result.getNumberOfElements());
        assertEquals(2, result.getContent().size());
        assertEquals(pageable, result.getPageable());
    }

    @Test
    public void testFindLastBooking() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user = userRepository.save(user);

        User owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        Item item = new Item();
        item.setOwner(owner.getId());
        item.setAvailable(true);
        Item savedItem = itemRepository.save(item);

        LocalDateTime now = LocalDateTime.now();
        Booking b1 = new Booking();
        b1.setBooker(user);
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.WAITING);
        b1.setItem(item);
        bookingRepository.save(b1);

        Booking b2 = new Booking();
        b2.setBooker(user);
        b2.setStart(now.plusHours(1));
        b2.setEnd(now.plusHours(2));
        b2.setStatus(BookingStatus.WAITING);
        b2.setItem(item);
        bookingRepository.save(b2);

        Booking actualBookingDate = bookingRepository.findLastBooking(savedItem.getId(), now);

        assertEquals(actualBookingDate.getBooker().getId(), b1.getBooker().getId());
        assertEquals(actualBookingDate.getStart(), b1.getStart());
    }

    @Test
    public void testFindAllLastBooking() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user = userRepository.save(user);

        User owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        Item item = new Item();
        item.setOwner(owner.getId());
        item.setAvailable(true);
        itemRepository.save(item);

        Item item2 = new Item();
        item2.setOwner(owner.getId());
        item2.setAvailable(true);
        itemRepository.save(item2);

        LocalDateTime now = LocalDateTime.now();
        Booking b1 = new Booking();
        b1.setBooker(user);
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.APPROVED);
        b1.setItem(item);
        bookingRepository.save(b1);

        Booking b2 = new Booking();
        b2.setBooker(user);
        b2.setStart(now.plusHours(1));
        b2.setEnd(now.plusHours(3));
        b2.setStatus(BookingStatus.APPROVED);
        b2.setItem(item);
        bookingRepository.save(b2);

        List<Booking> actualBookingDate = bookingRepository.findAllLastBooking(List.of(item.getId(), item2.getId()), now);

        assertEquals(actualBookingDate.size(), 1);
        assertEquals(actualBookingDate.get(0).getBooker().getId(), b1.getBooker().getId());
    }

    @Test
    public void testFindNextBooking() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user = userRepository.save(user);

        User owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        Item item = new Item();
        item.setOwner(owner.getId());
        item.setAvailable(true);
        Item savedItem = itemRepository.save(item);

        LocalDateTime now = LocalDateTime.now();
        Booking b1 = new Booking();
        b1.setBooker(user);
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.WAITING);
        b1.setItem(item);
        bookingRepository.save(b1);

        Booking b2 = new Booking();
        b2.setBooker(user);
        b2.setStart(now.plusHours(1));
        b2.setEnd(now.plusHours(2));
        b2.setStatus(BookingStatus.WAITING);
        b2.setItem(item);
        bookingRepository.save(b2);

        Booking actualBookingDate = bookingRepository.findNextBooking(savedItem.getId(), now);

        assertEquals(actualBookingDate.getBooker().getId(), b2.getBooker().getId());
        assertEquals(actualBookingDate.getStart(), b2.getStart());
    }

    @Test
    public void testFindAllNextBooking() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user = userRepository.save(user);

        User owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        Item item = new Item();
        item.setOwner(owner.getId());
        item.setAvailable(true);
        itemRepository.save(item);

        Item item2 = new Item();
        item2.setOwner(owner.getId());
        item2.setAvailable(true);
        itemRepository.save(item2);

        LocalDateTime now = LocalDateTime.now();
        Booking b1 = new Booking();
        b1.setBooker(user);
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.APPROVED);
        b1.setItem(item);
        bookingRepository.save(b1);

        Booking b2 = new Booking();
        b2.setBooker(user);
        b2.setStart(now.plusHours(1));
        b2.setEnd(now.plusHours(3));
        b2.setStatus(BookingStatus.APPROVED);
        b2.setItem(item);
        bookingRepository.save(b2);

        Booking b3 = new Booking();
        b3.setBooker(user);
        b3.setStart(now.minusHours(7));
        b3.setEnd(now.minusHours(6));
        b3.setStatus(BookingStatus.APPROVED);
        b3.setItem(item2);
        bookingRepository.save(b3);

        List<Booking> actualBookingDate = bookingRepository.findAllNextBooking(List.of(item.getId(), item2.getId()), now);

        assertEquals(actualBookingDate.size(), 1);
        assertEquals(actualBookingDate.get(0).getBooker().getId(), b2.getBooker().getId());
    }

    @Test
    public void testExistsBookingByBooker_IdAndItem_IdAndStatusAndStartBefore_shouldReturnTrue() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);

        User user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        userRepository.save(user);

        Item item = new Item();
        item.setOwner(4L);
        item.setAvailable(true);
        itemRepository.save(item);

        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(startDate);
        bookingRepository.save(booking);

        boolean exists = bookingRepository.existsBookingByBooker_IdAndItem_IdAndStatusAndStartBefore(user.getId(), item.getId(), BookingStatus.APPROVED, LocalDateTime.now());

        assertTrue(exists);
    }

    @Test
    public void testExistsBookingByBooker_IdAndItem_IdAndStatusAndStartBefore_shouldReturnFalse() {
        long userId = 1L;
        long itemId = 2L;
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        BookingStatus status = BookingStatus.APPROVED;
        User user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        userRepository.save(user);
        User owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);
        Item item = new Item();
        item.setOwner(owner.getId());
        item.setAvailable(true);
        itemRepository.save(item);
        Booking booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(status);
        booking.setStart(startDate);
        bookingRepository.save(booking);

        boolean exists = bookingRepository.existsBookingByBooker_IdAndItem_IdAndStatusAndStartBefore(userId, itemId, status, LocalDateTime.now());

        assertFalse(exists);
    }
}