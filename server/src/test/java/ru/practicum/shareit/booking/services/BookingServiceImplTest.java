package ru.practicum.shareit.booking.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.booking.dto.ReceivedBookingDto;
import ru.practicum.shareit.booking.dto.SentBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositories.BookingRepository;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.services.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.services.UserService;
import ru.practicum.shareit.util.BookingStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class})
public class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ItemService itemService;
    @Mock
    private UserService userService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private static final long BOOKING_ID = 1L;
    private static final long USER_ID = 2L;

    @Test
    public void testGetBookingReturnsBookingDto_success() {
        User user = new User();
        user.setId(USER_ID);
        Booking booking = new Booking();
        booking.setId(BOOKING_ID);
        booking.setBooker(user);
        SentBookingDto bookingDto = new SentBookingDto();
        bookingDto.setId(BOOKING_ID);
        bookingDto.setBooker(user);

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(modelMapper.map(booking, SentBookingDto.class)).thenReturn(bookingDto);

        SentBookingDto result = bookingService.getBooking(BOOKING_ID, USER_ID);

        assertNotNull(result);
    }

    @Test()
    public void testGetBookingThrowsEntityNotFoundWhenEntityNotFound() {
        long invalidId = -1;
        assertThrows(EntityNotFound.class, () -> bookingService.getBooking(invalidId, USER_ID));
    }

    @Test
    public void testGetBookingThrowsInappropriateUserWhenUserIsNotAuthorized() {
        User booker = new User();
        booker.setId(3L);
        User owner = new User();
        owner.setId(4L);
        Item item = new Item();
        item.setOwner(1L);
        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setId(BOOKING_ID);

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        assertThrows(InappropriateUser.class, () -> bookingService.getBooking(BOOKING_ID, USER_ID));
    }

    @Test
    public void testGetAllUserBookingsUnknownState() {
        long userId = 1L;
        String state = "INVALID";
        String userType = "USER";
        Integer from = 0;
        Integer size = 10;


        assertThrows(UnsupportedStatus.class, () -> bookingService.getAllUserBookings(userId, state, userType, from, size));
    }

    @Test
    public void testGetAllUserBookings_shouldThrowEntityNotFound() {
        long invalidUserId = 100L;
        String state = "ALL";
        String userType = "USER";
        Integer from = 0;
        Integer size = 10;

        doThrow(new EntityNotFound("User not found")).when(userService).isExistUser(invalidUserId);

        assertThrows(EntityNotFound.class, () -> bookingService.getAllUserBookings(invalidUserId, state, userType, from, size));
    }

    @Test
    public void testGetAllUserBookings_withPagination_shouldThrowBadRequest() {
        long invalidUserId = 100L;
        String state = "ALL";
        String userType = "USER";
        Integer from = 0;
        Integer size = 0;

        doNothing().when(userService).isExistUser(invalidUserId);

        assertThrows(BadRequest.class, () -> bookingService.getAllUserBookings(invalidUserId, state, userType, from, size));
    }

    @Test
    public void testGetAllUserBookings_noPagination_success() {
        long userId = 1L;
        Integer from = null;
        Integer size = null;
        String state = "ALL";
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = new ArrayList<>();
        Booking b1 = new Booking();
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.WAITING);
        bookings.add(b1);

        Booking b2 = new Booking();
        b2.setStart(now.plusHours(1));
        b2.setEnd(now.plusHours(3));
        b2.setStatus(BookingStatus.APPROVED);
        bookings.add(b2);

        when(bookingRepository.findAllUserBookingsByState(eq(userId), eq(state), any(LocalDateTime.class))).thenReturn(bookings);

        List<SentBookingDto> result = bookingService.getAllUserBookings(userId, state, "USER", from, size);

        assertEquals(2, result.size());
        verify(bookingRepository, times(1)).findAllUserBookingsByState(eq(userId), eq(state), any(LocalDateTime.class));
    }

    @Test
    public void testGetAllOwnerBookings_noPagination_success() {
        long userId = 1L;
        Integer from = null;
        Integer size = null;
        String state = "ALL";
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = new ArrayList<>();
        Booking b1 = new Booking();
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.WAITING);
        bookings.add(b1);

        Booking b2 = new Booking();
        b2.setStart(now.plusHours(1));
        b2.setEnd(now.plusHours(3));
        b2.setStatus(BookingStatus.APPROVED);
        bookings.add(b2);

        when(bookingRepository.findAllOwnerBookingsByState(eq(userId), eq(state), any(LocalDateTime.class))).thenReturn(bookings);

        List<SentBookingDto> result = bookingService.getAllUserBookings(userId, state, "OWNER", from, size);

        assertEquals(2, result.size());
        verify(bookingRepository, times(1)).findAllOwnerBookingsByState(eq(userId), eq(state), any(LocalDateTime.class));
    }

    @Test
    public void testGetAllUserBookings_withPagination_success() {
        long userId = 1L;
        int from = 0;
        int size = 1;
        String state = "ALL";
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = new ArrayList<>();
        Booking b1 = new Booking();
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.WAITING);
        bookings.add(b1);

        Booking b2 = new Booking();
        b2.setStart(now.plusHours(1));
        b2.setEnd(now.plusHours(3));
        b2.setStatus(BookingStatus.APPROVED);
        bookings.add(b2);



        Slice<Booking> requestPage = new PageImpl<>(bookings);
        when(bookingRepository.findAllUserBookingsByState(eq(userId), eq(state), any(LocalDateTime.class), eq(PageRequest.of(from, size)))).thenReturn(requestPage);

        List<SentBookingDto> result = bookingService.getAllUserBookings(userId, state, "USER", from, size);

        assertEquals(2, result.size());
        verify(bookingRepository, times(1)).findAllUserBookingsByState(eq(userId), eq(state), any(LocalDateTime.class), eq(PageRequest.of(from, size)));
    }

    @Test
    public void testGetAllOwnerBookings_withPagination_success() {
        long userId = 1L;
        int from = 0;
        int size = 1;
        String state = "ALL";
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = new ArrayList<>();
        Booking b1 = new Booking();
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.WAITING);
        bookings.add(b1);

        Booking b2 = new Booking();
        b2.setStart(now.plusHours(1));
        b2.setEnd(now.plusHours(3));
        b2.setStatus(BookingStatus.APPROVED);
        bookings.add(b2);


        Slice<Booking> requestPage = new PageImpl<>(bookings);
        when(bookingRepository.findAllOwnerBookingsByState(eq(userId), eq(state), any(LocalDateTime.class), eq(PageRequest.of(from, size)))).thenReturn(requestPage);

        List<SentBookingDto> result = bookingService.getAllUserBookings(userId, state, "OWNER", from, size);

        assertEquals(2, result.size());
        verify(bookingRepository, times(1)).findAllOwnerBookingsByState(eq(userId), eq(state), any(LocalDateTime.class), eq(PageRequest.of(from, size)));
    }

    @Test
    public void testGetAllPastOwnerBookings_withPagination_success() {
        long userId = 1L;
        int from = 0;
        int size = 1;
        String state = "PAST";
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = new ArrayList<>();
        Booking b1 = new Booking();
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.WAITING);
        bookings.add(b1);

        Booking b2 = new Booking();
        b2.setStart(now.plusHours(1));
        b2.setEnd(now.plusHours(3));
        b2.setStatus(BookingStatus.APPROVED);
        bookings.add(b2);


        Slice<Booking> requestPage = new PageImpl<>(List.of(b1));
        when(bookingRepository.findAllOwnerBookingsByState(eq(userId), eq(state), any(LocalDateTime.class), eq(PageRequest.of(from, size)))).thenReturn(requestPage);

        List<SentBookingDto> result = bookingService.getAllUserBookings(userId, state, "OWNER", from, size);

        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findAllOwnerBookingsByState(eq(userId), eq(state), any(LocalDateTime.class), eq(PageRequest.of(from, size)));
    }

    @Test
    public void testGetAllFutureOwnerBookings_withPagination_success() {
        long userId = 1L;
        int from = 0;
        int size = 1;
        String state = "FUTURE";
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = new ArrayList<>();
        Booking b1 = new Booking();
        b1.setStart(now.minusHours(2));
        b1.setEnd(now.minusHours(1));
        b1.setStatus(BookingStatus.WAITING);
        bookings.add(b1);

        Booking b2 = new Booking();
        b2.setStart(now.plusHours(1));
        b2.setEnd(now.plusHours(3));
        b2.setStatus(BookingStatus.APPROVED);
        bookings.add(b2);


        Slice<Booking> requestPage = new PageImpl<>(List.of(b2));
        when(bookingRepository.findAllOwnerBookingsByState(eq(userId), eq(state), any(LocalDateTime.class), eq(PageRequest.of(from, size)))).thenReturn(requestPage);

        List<SentBookingDto> result = bookingService.getAllUserBookings(userId, state, "OWNER", from, size);

        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findAllOwnerBookingsByState(eq(userId), eq(state), any(LocalDateTime.class), eq(PageRequest.of(from, size)));
    }

    @Test
    public void testCreateBooking_shouldThrowBadRequest() {
        ReceivedBookingDto bookingDto = new ReceivedBookingDto();
        Item item = new Item();
        item.setId(1L);
        item.setOwner(1L);
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(2));
        bookingDto.setEnd(LocalDateTime.now().minusHours(5));
        long userId = 2L;

        final BadRequest exception = assertThrows(BadRequest.class,
                () -> bookingService.createBooking(bookingDto, userId));

        assertEquals(exception.getMessage(), "Not valid fields");
    }

    @Test
    public void testCreateBooking_withNullBookingDate_shouldThrowBadRequest() {
        ReceivedBookingDto bookingDto = new ReceivedBookingDto();
        Item item = new Item();
        item.setId(1L);
        item.setOwner(1L);
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(null);
        bookingDto.setEnd(null);
        long userId = 2L;

        final BadRequest exception = assertThrows(BadRequest.class,
                () -> bookingService.createBooking(bookingDto, userId));

        assertEquals(exception.getMessage(), "Not valid fields");
    }

    @Test
    public void testCreateBooking_shouldThrowInappropriateUserRequest() {
        ReceivedBookingDto bookingDto = new ReceivedBookingDto();
        Item item = new Item();
        item.setId(1L);
        item.setOwner(1L);
        item.setAvailable(true);
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(2));
        bookingDto.setEnd(LocalDateTime.now().plusHours(5));
        long userId = 1L;

        when(itemService.getItemById(bookingDto.getItemId())).thenReturn(item);

        final InappropriateUser exception = assertThrows(InappropriateUser.class,
                () -> bookingService.createBooking(bookingDto, userId));

        assertEquals(exception.getMessage(), "Owner cant booking own item");
    }

    @Test
    public void testCreateBooking_shouldThrowItemIsUnavailable() {
        ReceivedBookingDto bookingDto = new ReceivedBookingDto();
        Item item = new Item();
        item.setId(1L);
        item.setOwner(1L);
        item.setAvailable(false);
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(2));
        bookingDto.setEnd(LocalDateTime.now().plusHours(5));
        long userId = 1L;

        when(itemService.getItemById(bookingDto.getItemId())).thenReturn(item);

        final ItemIsUnavailable exception = assertThrows(ItemIsUnavailable.class,
                () -> bookingService.createBooking(bookingDto, userId));

        assertEquals(exception.getMessage(), "Item " + item.getId() + "is unavailable");
    }

    @Test
    public void testCreateBooking_shouldThrowEntityNotFound() {
        ReceivedBookingDto bookingDto = new ReceivedBookingDto();
        Item item = new Item();
        item.setId(1L);
        item.setOwner(1L);
        item.setAvailable(false);
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(2));
        bookingDto.setEnd(LocalDateTime.now().plusHours(5));
        long userId = 1L;

        doThrow(new EntityNotFound("Item not found: " + item.getId())).when(itemService).getItemById(item.getId());

        final EntityNotFound exception = assertThrows(EntityNotFound.class,
                () -> bookingService.createBooking(bookingDto, userId));

        assertEquals(exception.getMessage(), "Item not found: " + item.getId());
    }

    @Test
    public void testCreateBooking_success() {
        ReceivedBookingDto bookingDto = new ReceivedBookingDto();
        Item item = new Item();
        item.setId(1L);
        item.setOwner(1L);
        item.setAvailable(true);
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(2));
        bookingDto.setEnd(LocalDateTime.now().plusHours(5));
        long userId = 2L;
        User user = new User();
        user.setId(userId);
        Booking booking = new Booking();
        booking.setEnd(bookingDto.getEnd());
        booking.setStart(bookingDto.getStart());


        when(itemService.getItemById(bookingDto.getItemId())).thenReturn(item);
        when(userService.getUserById(userId)).thenReturn(user);
        when(modelMapper.map(bookingDto, Booking.class)).thenReturn(booking);


        bookingService.createBooking(bookingDto, userId);

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    public void testUpdateBookingStatus_withValidRequest_returnsSentBookingDtoApproved() {
        long bookingId = 1L;
        String approved = "true";
        long userId = 2L;

        Item item = new Item();
        item.setId(1L);
        item.setOwner(userId);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        SentBookingDto updatedBooking = new SentBookingDto();
        updatedBooking.setId(bookingId);
        updatedBooking.setItem(item);
        updatedBooking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(modelMapper.map(booking, SentBookingDto.class)).thenReturn(updatedBooking);

        SentBookingDto sentBookingDto = bookingService.updateBookingStatus(bookingId, approved, userId);

        verify(bookingRepository).save(any(Booking.class));
        assertEquals(bookingId, sentBookingDto.getId());
        assertEquals("APPROVED", sentBookingDto.getStatus().name());
    }

    @Test
    public void testUpdateBookingStatus_withValidRequest_returnsSentBookingDtoRejected() {
        long bookingId = 1L;
        String approved = "false";
        long userId = 2L;

        Item item = new Item();
        item.setId(1L);
        item.setOwner(userId);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        SentBookingDto updatedBooking = new SentBookingDto();
        updatedBooking.setId(bookingId);
        updatedBooking.setItem(item);
        updatedBooking.setStatus(BookingStatus.REJECTED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(modelMapper.map(booking, SentBookingDto.class)).thenReturn(updatedBooking);

        SentBookingDto sentBookingDto = bookingService.updateBookingStatus(bookingId, approved, userId);

        verify(bookingRepository).save(any(Booking.class));
        assertEquals(bookingId, sentBookingDto.getId());
        assertEquals("REJECTED", sentBookingDto.getStatus().name());
    }

    @Test
    public void testUpdateBookingStatus_withInvalidBookingId_throwsEntityNotFound() {
        long bookingId = 1L;
        String approved = "true";
        long userId = 2L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        final EntityNotFound exception = assertThrows(EntityNotFound.class,
                () -> bookingService.updateBookingStatus(bookingId, approved, userId));

        assertEquals(exception.getMessage(), "Booking not found: " + bookingId);
    }

    @Test
    public void testUpdateBookingStatus_withInappropriateUser_throwsInappropriateUser() {
        long bookingId = 1L;
        String approved = "true";
        long userId = 2L;

        Item item = new Item();
        item.setId(1L);
        item.setOwner(3L);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        final InappropriateUser exception = assertThrows(InappropriateUser.class,
                () -> bookingService.updateBookingStatus(bookingId, approved, userId));

        assertEquals(exception.getMessage(), "Inappropriate User: " + userId);
    }

    @Test
    public void testUpdateBookingStatus_withAlreadySetStatus_throwsBookingStatusAlreadySet() {
        long bookingId = 1L;
        String approved = "true";
        long userId = 2L;

        Item item = new Item();
        item.setId(1L);
        item.setOwner(userId);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        final BookingStatusAlreadySet exception = assertThrows(BookingStatusAlreadySet.class,
                () -> bookingService.updateBookingStatus(bookingId, approved, userId));

        assertEquals(exception.getMessage(), "Booking status already set: " + bookingId);
    }
}