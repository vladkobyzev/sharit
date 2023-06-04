package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.HashMap;
import java.util.Map;

@Service
public class RequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public RequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getRequestById(long requestId, long userId) {
        return get("/" + requestId, userId);
    }

    public ResponseEntity<Object> getUserRequests(long userId, Integer from, Integer size) {
        StringBuilder pathBuilder = new StringBuilder("/all");
        Map<String, Object> parameters = new HashMap<>();
        if (from != null && size != null) {
            pathBuilder.append("?from={from}&size={size}");
            parameters.put("from", from);
            parameters.put("size", size);
        }
        String path = pathBuilder.toString();
        return get(path, userId, parameters);
    }

    public ResponseEntity<Object> getOwnerRequests(long ownerId) {
        return get("", + ownerId);
    }

    public ResponseEntity<Object> createRequest(ItemRequestDto requestDto, long userId) {
        return post("", userId, requestDto);
    }
}
