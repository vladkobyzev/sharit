package ru.practicum.shareit.booking.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.booking.dto.ReceivedBookingDto;
import ru.practicum.shareit.booking.dto.SentBookingDto;
import ru.practicum.shareit.booking.services.BookingService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    private static final String USER_ID = "X-Sharer-User-Id";

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);


    @SneakyThrows
    @Test
    public void getBooking() {
        long bookingId = 1L;
        long userId = 1L;

        mockMvc.perform(get("/bookings/{id}", bookingId)
                        .header(USER_ID, String.valueOf(userId)))
                .andExpect(status().isOk());

        verify(bookingService).getBooking(bookingId, userId);
    }

    @Test
    public void testGetAllUserBookings() throws Exception {
        long userId = 1L;
        String state = "ALL";
        Integer from = 0;
        Integer size = 10;
        List<SentBookingDto> sentBookingDtoList = List.of(new SentBookingDto(), new SentBookingDto());

        when(bookingService.getAllUserBookings(userId, state, "USER", from, size))
                .thenReturn(sentBookingDtoList);

        MvcResult mvcResult = mockMvc.perform(get("/bookings")
                        .header(USER_ID, 1L)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .param("state", "ALL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<SentBookingDto> responseDtoList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        verify(bookingService).getAllUserBookings(userId, state, "USER", from, size);
        assertEquals(sentBookingDtoList, responseDtoList);
    }

    @Test
    public void testGetAllOwnerBookings() throws Exception {
        long userId = 1L;
        String state = "ALL";
        Integer from = 0;
        Integer size = 10;
        List<SentBookingDto> bookings = Arrays.asList(new SentBookingDto(), new SentBookingDto());

        when(bookingService.getAllUserBookings(eq(userId), eq(state), eq("OWNER"), eq(from), eq(size))).thenReturn(bookings);

        MvcResult mvcResult = mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID, userId)
                        .param("from", from.toString())
                        .param("size", size.toString())
                        .param("state", state)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<SentBookingDto> responseDtoList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        verify(bookingService).getAllUserBookings(userId, state, "OWNER", from, size);
        assertEquals(bookings, responseDtoList);
    }

    @Test
    public void createBooking() throws Exception {
        ReceivedBookingDto receivedBookingDtoTest = new ReceivedBookingDto();
        SentBookingDto sentBookingDto = new SentBookingDto();

        long userId = 1L;

        when(bookingService.createBooking(receivedBookingDtoTest, userId)).thenReturn(sentBookingDto);

        MvcResult mvcResult = mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID, String.valueOf(userId))
                        .content(objectMapper.writeValueAsString(receivedBookingDtoTest)))
                .andExpect(status().isOk())
                .andReturn();

        SentBookingDto responseDto = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), SentBookingDto.class);

        assertEquals(sentBookingDto, responseDto);

    }

    @SneakyThrows
    @Test
    public void updateBookingStatus() {
        long bookingId = 1L;
        String approved = "true";
        long userId = 2L;
        SentBookingDto sentBookingDto = new SentBookingDto();

        when(bookingService.updateBookingStatus(bookingId, approved, userId)).thenReturn(sentBookingDto);

        MvcResult mvcResult = mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", approved)
                        .header(USER_ID, userId))
                .andExpect(status().isOk())
                .andReturn();

        SentBookingDto responseDto = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), SentBookingDto.class);

        assertEquals(sentBookingDto, responseDto);
    }
}