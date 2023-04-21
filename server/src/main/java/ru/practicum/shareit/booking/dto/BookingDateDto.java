package ru.practicum.shareit.booking.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDateDto {
    private Long id;

    private LocalDateTime bookingDate;

    private Long bookerId;
}
