package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.UserServiceImpl;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        log.info("SERVER: Create for: {} - Started", userDto);
        log.info("SERVER: create: {} - Server finished", userService.create(userDto));
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable("userId") long id,
                          @RequestBody UserDto userDto) {
        log.info("SERVER: update {} for user id: {}  - Started", userDto, id);
        log.info("SERVER: update: {} - Finished", userService.update(userDto, id));
        return userService.update(userDto, id);
    }

    @GetMapping
    public List<UserDto> findAll() {
        log.info("SERVER: findAll on Server - Started");
        log.info("SERVER: findAll: найдено {} пользователей - Finished", userService.getUsers().size());
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable("userId") long id) {
        log.info("SERVER: getUser: {} - Started", id);
        log.info("SERVER: getUser: {} - Finished", userService.getUser(id));
        return userService.getUser(id);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") Integer userId) {
        log.info("SERVER: deleteUser: {} userId - Started", userId);
        log.info("SERVER: deleteUser: {} userId - Finished {} ", userId, userService.delete(userId));
    }
}