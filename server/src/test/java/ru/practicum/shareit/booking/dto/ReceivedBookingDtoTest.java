package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
public class ReceivedBookingDtoTest {
    @Autowired
    private JacksonTester<ReceivedBookingDto> json;

    @Test
    public void testJsonFormat() throws IOException {
        String jsonInput = "{\"itemId\":1,\"start\":\"2023-04-05T14:30:00\",\"end\":\"2023-04-05T16:30:00\"}";

        ReceivedBookingDto receivedBookingDtoTest = json.parse(jsonInput).getObject();

        LocalDateTime start = LocalDateTime.parse("2023-04-05T14:30:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        LocalDateTime end = LocalDateTime.parse("2023-04-05T16:30:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        assertEquals(start, receivedBookingDtoTest.getStart());
        assertEquals(end, receivedBookingDtoTest.getEnd());

        String jsonString = json.write(receivedBookingDtoTest).getJson();

        assertEquals(jsonInput, jsonString);
    }

    @Test
    public void testInvalidJsonFormat() {
        String jsonInput = "{\"itemId\":1,\"start\":\"2023-04-05\",\"end\":\"2023-04-05T16:30:00\"}";

        try {
            json.parse(jsonInput).getObject();
        } catch (IOException e) {
            assertEquals(InvalidFormatException.class, e.getClass());
        }
    }
}
