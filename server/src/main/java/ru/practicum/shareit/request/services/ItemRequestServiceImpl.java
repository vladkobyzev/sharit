package ru.practicum.shareit.request.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.BadRequest;
import ru.practicum.shareit.exceptions.EntityNotFound;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repositories.RequestRepository;
import ru.practicum.shareit.user.services.UserService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ModelMapper mapper;
    private final UserService userService;
    private final RequestRepository requestRepository;

    @Autowired
    public ItemRequestServiceImpl(ModelMapper mapper, UserService userService, RequestRepository requestRepository) {
        this.mapper = mapper;
        this.userService = userService;
        this.requestRepository = requestRepository;
    }

    @Override
    public ItemRequestDto createRequest(ItemRequestDto requestDto, long userId) {
        userService.isExistUser(userId);
        ItemRequest request = convertDtoToRequest(requestDto);
        request.setOwner(userId);
        request.setCreated(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        return convertRequestToDto(requestRepository.save(request));
    }

    @Override
    public ItemRequestDto getRequestById(long requestId, long userId) {
        userService.isExistUser(userId);
        return convertRequestToDto(requestRepository.findById(requestId).orElseThrow(() -> new EntityNotFound("")));
    }

    @Override
    public List<ItemRequestDto> getOwnerRequests(long ownerId) {
        userService.isExistUser(ownerId);
        return convertListToDto(requestRepository.findAllByOwner(ownerId));
    }

    @Override
    public List<ItemRequestDto> getUserRequests(long userId, Integer from, Integer size) {
        if (from == null && size == null) {
            return convertListToDto(requestRepository.findAllByOwner(userId));
        }
        if ((from == 0 && size == 0) || (from < 0 || size < 0)) {
            throw new BadRequest("Request without pagination");
        }
        return convertListToDto(requestRepository.findAllByOwnerNot(userId, PageRequest.of(from, size, Sort.by("created").ascending())));
    }

    private ItemRequestDto convertRequestToDto(ItemRequest itemRequest) {
        return mapper.map(itemRequest, ItemRequestDto.class);
    }

    private ItemRequest convertDtoToRequest(ItemRequestDto itemRequestDto) {
        return mapper.map(itemRequestDto, ItemRequest.class);
    }

    private List<ItemRequestDto> convertListToDto(List<ItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(this::convertRequestToDto)
                .collect(Collectors.toList());
    }
}
