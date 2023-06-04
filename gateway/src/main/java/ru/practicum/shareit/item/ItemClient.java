package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getItemDtoById(long itemId, long userId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getItems(long userId, Integer from, Integer size) {
        String path = "";
        Map<String, Object> parameters = null;
        if (from != null && size != null) {
            parameters = Map.of(
                    "from", from,
                    "size", size
            );
            path = "?from={from}&size={size}";
        }
        return get(path, userId, parameters);
    }

    public ResponseEntity<Object> searchItemByText(String text) {
        Map<String, Object> parameters = Map.of(
                "text", text
        );
        return get("/search?text={text}", null, parameters);
    }

    public ResponseEntity<Object> createItem(ItemDto itemDto, long userId) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> createComment(CommentDto commentDto, long itemId, long userId) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }

    public ResponseEntity<Object> updateItem(ItemDto itemDto, long itemId, long userId) {
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> deleteItem(long id) {
        return delete("/" + id);
    }
}
