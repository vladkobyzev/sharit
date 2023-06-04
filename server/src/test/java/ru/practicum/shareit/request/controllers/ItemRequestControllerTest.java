package ru.practicum.shareit.request.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.exceptions.EntityNotFound;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.services.ItemRequestService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    private static final String USER_ID = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService requestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateRequest() throws Exception {
        long userId = 1L;
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Request Name");

        when(requestService.createRequest(requestDto, userId)).thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())));

        verify(requestService).createRequest(requestDto, userId);
    }

    @Test
    public void testGetOwnerRequests() throws Exception {
        int from = 0;
        int size = 2;
        long ownerId = 1L;
        ItemRequestDto requestDto1 = new ItemRequestDto();
        requestDto1.setId(1L);
        requestDto1.setDescription("Request 1");

        ItemRequestDto requestDto2 = new ItemRequestDto();
        requestDto2.setId(2L);
        requestDto2.setDescription("Request 2");

        List<ItemRequestDto> requestList = Arrays.asList(requestDto1, requestDto2);

        when(requestService.getOwnerRequests(ownerId)).thenReturn(requestList);

        MvcResult mvcResult = mockMvc.perform(get("/requests")
                        .header(USER_ID, ownerId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andReturn();

        List<ItemRequestDto> responseDtoList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertEquals(requestList, responseDtoList);
        verify(requestService).getOwnerRequests(ownerId);
    }

    @Test
    public void testGetUserRequests() throws Exception {
        long userId = 1L;
        int from = 0;
        int size = 2;
        ItemRequestDto requestDto1 = new ItemRequestDto();
        requestDto1.setId(1L);
        requestDto1.setDescription("Request 1");

        ItemRequestDto requestDto2 = new ItemRequestDto();
        requestDto2.setId(2L);
        requestDto2.setDescription("Request 2");

        List<ItemRequestDto> requestList = Arrays.asList(requestDto1, requestDto2);

        when(requestService.getUserRequests(userId, from, size)).thenReturn(requestList);

        MvcResult mvcResult = mockMvc.perform(get("/requests/all")
                        .header(USER_ID, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andReturn();
        List<ItemRequestDto> responseDtoList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(requestList, responseDtoList);
        verify(requestService).getUserRequests(userId, from, size);
    }

    @Test
    public void testGetRequestByIdSuccess() throws Exception {
        long requestId = 1L;
        long userId = 2L;

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(requestId);

        when(requestService.getRequestById(requestId, userId)).thenReturn(requestDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) requestId)));
    }

    @Test
    public void testGetRequestByIdNotFound() throws Exception {
        long requestId = 1L;
        long userId = 2L;

        when(requestService.getRequestById(requestId, userId)).thenThrow(new EntityNotFound("Entity not found"));

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID, userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Entity not found")));
    }
}