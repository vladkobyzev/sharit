package ru.practicum.shareit.item.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.services.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final long USER_ID = 1;
    private static final long ITEM_ID = 2;

    @SneakyThrows
    @Test
    public void testGetItemById() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(ITEM_ID);
        itemDto.setName("Test item");

        when(itemService.getItemDtoById(eq(ITEM_ID), eq(USER_ID))).thenReturn(itemDto);

        mockMvc.perform(get("/items/" + ITEM_ID).header(HEADER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) ITEM_ID)))
                .andExpect(jsonPath("$.name", is("Test item")));
    }

    @SneakyThrows
    @Test
    public void testGetItems() {
        ItemDto item1 = new ItemDto();
        item1.setId(ITEM_ID);
        item1.setName("Test item 1");

        ItemDto item2 = new ItemDto();
        item2.setId(ITEM_ID + 1);
        item2.setName("Test item 2");

        List<ItemDto> items = Arrays.asList(item1, item2);

        when(itemService.getItems(eq(USER_ID), eq(null), eq(null))).thenReturn(items);

        mockMvc.perform(get("/items").header(HEADER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is((int) ITEM_ID)))
                .andExpect(jsonPath("$[0].name", is("Test item 1")))
                .andExpect(jsonPath("$[1].id", is((int) ITEM_ID + 1)))
                .andExpect(jsonPath("$[1].name", is("Test item 2")));
    }

    @SneakyThrows
    @Test
    public void testSearchItemsByText() {
        ItemDto item1 = new ItemDto();
        item1.setId(ITEM_ID);
        item1.setName("Test item 1");

        ItemDto item2 = new ItemDto();
        item2.setId(ITEM_ID + 1);
        item2.setName("Test item 2");

        List<ItemDto> items = Arrays.asList(item1, item2);

        when(itemService.searchItemByText(anyString())).thenReturn(items);

        mockMvc.perform(get("/items/search").param("text", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is((int) ITEM_ID)))
                .andExpect(jsonPath("$[0].name", is("Test item 1")))
                .andExpect(jsonPath("$[1].id", is((int) ITEM_ID + 1)))
                .andExpect(jsonPath("$[1].name", is("Test item 2")));
    }

    @SneakyThrows
    @Test
    public void testCreateItem_Success() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Desc");
        itemDto.setAvailable(true);

        when(itemService.createItem(eq(itemDto), anyLong())).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(HEADER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andReturn();
    }

    @SneakyThrows
    @Test
    public void testCreateItem_InvalidItem() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("");

        mockMvc.perform(post("/items")
                        .header(HEADER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @SneakyThrows
    @Test
    public void testCreateComment_Success() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Test Comment");

        when(itemService.createComment(eq(commentDto), anyLong(), anyLong())).thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(HEADER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andReturn();
    }

    @SneakyThrows
    @Test
    public void testUpdateItem_Success() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");

        when(itemService.updateItem(eq(itemDto), anyLong(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .header(HEADER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andReturn();
    }

    @SneakyThrows
    @Test
    public void testDeleteItem_Success() {
        mockMvc.perform(delete("/items/1")
                        .header(HEADER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }
}