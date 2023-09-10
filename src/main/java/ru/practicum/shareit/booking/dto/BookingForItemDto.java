package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.booking.model.StatusOfBooking;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class BookingForItemDto {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemDto item;
    private Long bookerId;
    private StatusOfBooking status;

    public BookingForItemDto(long id, LocalDateTime start, LocalDateTime end, ItemDto item, Long bookerId, StatusOfBooking status) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.item = item;
        this.bookerId = bookerId;
        this.status = status;
    }

    public BookingForItemDto() {
    }
}
