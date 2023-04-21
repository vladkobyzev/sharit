package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getAllUserBookings(long userId, String state, Integer from, Integer size) {
        StringBuilder pathBuilder = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();

        setParameters(pathBuilder, parameters, state, from, size);

        String path = pathBuilder.toString();
        return get(path, userId, parameters);
    }


    public ResponseEntity<Object> createBooking(long userId, BookItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getBooking(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getOwnerBookings(long userId, String state, Integer from, Integer size) {
        StringBuilder pathBuilder = new StringBuilder("/owner");
        Map<String, Object> parameters = new HashMap<>();

        setParameters(pathBuilder, parameters, state, from, size);

        String path = pathBuilder.toString();
        return get(path, userId, parameters);
    }

    public ResponseEntity<Object> updateBookingStatus(long bookingId, String approved, long userId) {
        Map<String, Object> parameters = Map.of(
                "approved", approved
        );
        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }

    private void setParameters(StringBuilder pathBuilder, Map<String, Object> parameters, String state, Integer from, Integer size) {
        if (state != null) {
            pathBuilder.append("?state={state}");
            parameters.put("state", state);
        }

        if (from != null && size != null) {
            String separator = pathBuilder.toString().contains("?") ? "&" : "?";
            pathBuilder.append(separator).append("from={from}&size={size}");
            parameters.put("from", from);
            parameters.put("size", size);
        }
    }
}
