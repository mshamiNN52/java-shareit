package ru.practicum.shareit.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserService;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ItemControllerTest {
    ItemController controller;
    @Autowired
    ItemService itemService;
    @Autowired
    UserService userService;
    UserDto userDto = new UserDto();
    User firstUser = new User();

    @BeforeEach
    public void beforeEach() {
        controller = new ItemController(itemService);
        userDto.setName("First");
        userDto.setEmail("aa1@a.mail.ru");
        firstUser = UserMapper.makeUserWithId(userService.create(userDto))
                .orElseThrow(() -> new NullPointerException("User объект не создан"));
    }

    @AfterEach
    public void afterEach() {
        controller.clearAll();
        userService.clearAll();
    }
}