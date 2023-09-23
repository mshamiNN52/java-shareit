package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingService;
import ru.practicum.shareit.booking.model.StateForBooking;

import java.util.List;

import static ru.practicum.shareit.utils.HeaderUserIdConst.HEADER_USER_ID;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponseDto add(@RequestHeader(HEADER_USER_ID) Long bookerId,
                                  @RequestBody BookingRequestDto bookingDto) {
        log.info("SERVER: Add new booking: {} - Started", bookingDto);
        log.info("SERVER: Create booking: {} - Finished", bookingService.addNewBooking(bookerId, bookingDto));
        return bookingService.addNewBooking(bookerId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                      @PathVariable Long bookingId,
                                      @RequestParam Boolean approved) {
        log.info("SERVER: Set status {} for booking id: {} by user id {}  - Started", approved, bookingId, ownerId);
        log.info("SERVER: Set status: {} - Finished", bookingService.approveBooking(ownerId, bookingId, approved).getStatus());
        return bookingService.approveBooking(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBooking(@RequestHeader(HEADER_USER_ID) Long userId,
                                         @PathVariable Long bookingId) {
        log.info("SERVER: Search for booking id {} - Started", bookingId);
        log.info("SERVER: Booking {} was found", bookingService.getBooking(bookingId, userId));
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getBookings(@RequestHeader(HEADER_USER_ID) Long bookerId,
                                                @RequestParam(defaultValue = "ALL") StateForBooking state,
                                                @RequestParam(required = false, defaultValue = "0") Integer from,
                                                @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("SERVER: Search user's (id {}) {} bookings - Started", bookerId, state);
        log.info("SERVER: {} {} bookings was found", bookingService.getBookings(bookerId, state, from, size).size(), state);
        return bookingService.getBookings(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsOwner(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                                     @RequestParam(defaultValue = "ALL") StateForBooking state,
                                                     @RequestParam(required = false, defaultValue = "0") Integer from,
                                                     @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("SERVER: Search {} bookings of owner's (id {}) items - Started", state, ownerId);
        log.info("SERVER: {} {} bookings was found", state, bookingService.getBookingsForOwner(ownerId, state, from, size).size());
        return bookingService.getBookingsForOwner(ownerId, state, from, size);
    }
}