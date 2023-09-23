package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestService;

import java.util.List;

import static ru.practicum.shareit.utils.HeaderUserIdConst.HEADER_USER_ID;

@Validated
@RestController
@RequestMapping(path = "/requests")
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    ItemRequestDto addRequest(@RequestHeader(HEADER_USER_ID) Long requesterId,
                              @RequestBody ItemRequestDto itemRequestDto) {
        log.info("SERVER: Add new request: {} by user id {} - Started", itemRequestDto, requesterId);
        log.info("SERVER: Add new request: {} - Finished", itemRequestService.addNewItemRequest(requesterId, itemRequestDto));
        return itemRequestService.addNewItemRequest(requesterId, itemRequestDto);
    }

    @GetMapping
    List<ItemRequestDto> getItemRequests(@RequestHeader(HEADER_USER_ID) Long requesterId) {
        log.info("SERVER: Get requests for user id: {} - Started", requesterId);
        log.info("SERVER: Size of founded List of requests is {} - Finished", itemRequestService.getItemRequests(requesterId).size());
        return itemRequestService.getItemRequests(requesterId);
    }

    @GetMapping("/all")
    List<ItemRequestDto> getAllItemRequests(@RequestHeader(HEADER_USER_ID) Long userId,
                                            @RequestParam(required = false, defaultValue = "0") Integer from,
                                            @RequestParam(required = false, defaultValue = "20") Integer size) {

        log.info("SERVER: Get All requests - Started");
        log.info("SERVER: Size of founded List of requests is {} - Finished", itemRequestService.getAllItemRequests(userId, from, size).size());
        return itemRequestService.getAllItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    ItemRequestDto getItemRequest(@RequestHeader(HEADER_USER_ID) Long userId,
                                  @PathVariable Long requestId) {
        log.info("SERVER: Get request id: {} - Started", requestId);
        log.info("SERVER: Request id {} was found - Finished", requestId);
        return itemRequestService.getItemRequest(userId, requestId);
    }
}
