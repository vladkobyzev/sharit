package ru.practicum.shareit.booking.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.ReceivedBookingDto;
import ru.practicum.shareit.booking.dto.SentBookingDto;
import ru.practicum.shareit.booking.services.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String USER_ID = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }


    @GetMapping("/{bookingId}")
    public SentBookingDto getBooking(@PathVariable long bookingId,
                                     @RequestHeader(value = USER_ID) long userId) {
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping()
    public List<SentBookingDto> getAllUserBookings(@RequestHeader(value = USER_ID) long userId,
                                                   @RequestParam(name = "from", required = false) Integer from,
                                                   @RequestParam(name = "size", required = false) Integer size,
                                                   @RequestParam(name = "state", required = false, defaultValue = "ALL")
                                                   String state) {
        return bookingService.getAllUserBookings(userId, state, "USER", from, size);
    }

    @GetMapping("/owner")
    public List<SentBookingDto> getAllOwnerBookings(@RequestHeader(value = USER_ID) long userId,
                                                    @RequestParam(name = "from", required = false) Integer from,
                                                    @RequestParam(name = "size", required = false) Integer size,
                                                    @RequestParam(name = "state",
                                                            required = false, defaultValue = "ALL") String state) {
        return bookingService.getAllUserBookings(userId, state, "OWNER", from, size);
    }

    @PostMapping()
    public SentBookingDto createBooking(@RequestBody ReceivedBookingDto bookingDto,
                                        @RequestHeader(value = USER_ID) long userId) {
        return bookingService.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public SentBookingDto updateBookingStatus(@PathVariable long bookingId,
                                              @RequestParam(name = "approved") String approved,
                                              @RequestHeader(value = USER_ID) long userId) {
        return bookingService.updateBookingStatus(bookingId, approved.toLowerCase(), userId);
    }
}
