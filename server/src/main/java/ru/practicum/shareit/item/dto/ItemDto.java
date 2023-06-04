package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.BookingDate;
import ru.practicum.shareit.item.model.Comment;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class ItemDto {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private Boolean available;

    private BookingDate lastBooking;
    private BookingDate nextBooking;

    private Set<Comment> comments;

    private Long requestId;

}
