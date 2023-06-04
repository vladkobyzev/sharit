package ru.practicum.shareit.booking.services;

import ru.practicum.shareit.booking.dto.ReceivedBookingDto;
import ru.practicum.shareit.booking.dto.SentBookingDto;

import java.util.List;


public interface BookingService {

    SentBookingDto getBooking(long bookingId, long userId);

    List<SentBookingDto> getAllUserBookings(long userId, String state, String user, Integer from, Integer size);

    SentBookingDto createBooking(ReceivedBookingDto bookingDto, long userId);

    SentBookingDto updateBookingStatus(long bookingId, String approved, long userId);
}
