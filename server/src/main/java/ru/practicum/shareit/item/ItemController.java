package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.ItemService;

import java.util.Collection;
import java.util.List;

import static ru.practicum.shareit.utils.HeaderUserIdConst.HEADER_USER_ID;

@RestController
@RequestMapping(path = "/items")
@Slf4j
public class ItemController {
    private ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(HEADER_USER_ID) Long authorId,
                                 @PathVariable Long itemId,
                                 @RequestBody CommentRequestDto requestDto) {
        log.info("SERVER: Add new comment {} to item: {} - Started", requestDto, itemId);
        requestDto.setAuthorId(authorId);
        requestDto.setItemId(itemId);
        log.info("SERVER: Comment added to item id: {} - Finished", itemId);
        return itemService.addNewCommentToItem(requestDto);
    }

    @PostMapping
    public ItemDto add(@RequestHeader(HEADER_USER_ID) Long ownerId,
                       @RequestBody ItemDto itemDto) {
        log.info("SERVER: add: {} - Started", itemDto);
        log.info("SERVER: create: {} - Finished", itemService.addNewItem(ownerId, itemDto));
        return itemService.addNewItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(HEADER_USER_ID) Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("SERVER: Update {} for item id: {} by user id {}  - Started", itemDto, itemId, userId);
        log.info("SERVER: update: {} - Finished", itemService.updateItem(userId, itemId, itemDto));
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader(HEADER_USER_ID) Long ownerId) {
        log.info("SERVER: GetItems for user id {} - Started", ownerId);
        log.info("SERVER: Found {} items of user id {} - GetItems Finished", itemService.getItems(ownerId).size(), ownerId);
        return itemService.getItems(ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader(HEADER_USER_ID) Long userId,
                           @PathVariable Long itemId) {
        log.info("SERVER: Search for item id {} - Started", itemId);
        log.info("SERVER: item {} was found", itemService.getItem(itemId, userId));
        return itemService.getItem(itemId, userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItems(@RequestParam String text) {
        log.info("SERVER: Search for available items with text '{}' - Started", text.toLowerCase());
        log.info("SERVER: {} available items with text '{}' was found - Finished", itemService.searchForItems(text).size(), text.toLowerCase());
        return itemService.searchForItems(text);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader(HEADER_USER_ID) Long userId,
                           @PathVariable Long itemId) {
        log.info("SERVER: Delete item id {} user id {} - Started", itemId, userId);
        itemService.deleteItem(userId, itemId);
        log.info("SERVER: item id {} was deleted", itemId);
    }

    @DeleteMapping
    public void clearAll() {
        log.info("SERVER: Total clear - Started");
        itemService.clearAll();
        log.info("SERVER: All items was deleted");
    }
}
