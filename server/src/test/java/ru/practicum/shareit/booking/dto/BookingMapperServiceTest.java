package ru.practicum.shareit.booking.dto;

import  org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StateForBooking;
import ru.practicum.shareit.booking.model.StatusOfBooking;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemService;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingMapperServiceTest {
    private final Long ownerId1 = 1L;
    private final Long bookerId2 = 2L;
    private LocalDateTime start = LocalDateTime.now().plusDays(1);
    private LocalDateTime end = start.plusDays(2);
    private User userOwner;
    private User userBooker;
    private Item item;
    private Booking bookingForSave;
    private Booking newBooking;
    private BookingRequestDto newBookingRequestDto;
    private BookingResponseDto bookingResponseDtoFromRepo;
    private Booking approvedBooking;
    private Booking rejectedBooking;

    @Mock
    private ItemService itemService;
    @Mock
    private UserService userService;
    @Mock
    private BookingRepository bookingRepo;

    @InjectMocks
    private BookingMapperService bookingMapperService;

    @Captor
    private ArgumentCaptor<List<BookingResponseDto>> listBookingArgumentCaptor;

    @BeforeEach
    void setUp() {
        newBookingRequestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        userOwner = User.builder()
                .id(ownerId1)
                .email("oo@o.ru")
                .name("OwnerUser")
                .build();

        userBooker = User.builder()
                .id(bookerId2)
                .email("bb@b.ru")
                .name("BookerUser")
                .build();

        item = Item.builder()
                .description("item description")
                .isAvailable(true)
                .id(1L)
                .name("test item")
                .owner(userOwner)
                .build();

        bookingForSave = BookingMapper.requestDtoToEntity(newBookingRequestDto, item, userOwner).orElseThrow();

        newBooking = Booking.builder()
                .booker(userBooker)
                .id(1L)
                .status(StatusOfBooking.WAITING)
                .start(start)
                .end(end)
                .item(item)
                .build();

        approvedBooking = Booking.builder()
                .booker(userBooker)
                .id(1L)
                .status(StatusOfBooking.APPROVED)
                .start(start)
                .end(end)
                .item(item)
                .build();

        rejectedBooking = Booking.builder()
                .booker(userBooker)
                .id(1L)
                .status(StatusOfBooking.REJECTED)
                .start(start)
                .end(end)
                .item(item)
                .build();


        bookingResponseDtoFromRepo = BookingMapper.entityToResponseDto(newBooking).orElseThrow();
    }


    @Test
    void testAddStatusToBooking_whenApprovedIsTrue_thenReturnEntityWithStatusApproved() {
        when(bookingRepo.findById(1L)).thenReturn(Optional.ofNullable(newBooking));
        assertEquals(approvedBooking, bookingMapperService.addStatusToBooking(1L, 1L, true));
    }

    @Test
    void testAddStatusToBooking_whenApprovedIsFalse_thenReturnEntityWithStatusRejected() {
        when(bookingRepo.findById(1L)).thenReturn(Optional.ofNullable(newBooking));
        assertEquals(rejectedBooking, bookingMapperService.addStatusToBooking(1L, 1L, false));
    }

    @Test
    void testAddStatusToBooking_whenSecondaryApproved_thenThrowValidationException() {
        when(bookingRepo.findById(1L)).thenReturn(Optional.ofNullable(approvedBooking));
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> bookingMapperService.addStatusToBooking(1L, 1L, true));
        validationException.getMessage();
    }

    @Test
    void testAddStatusToBooking_whenApprovedIsNull2_thenThrowValidationException() {
        when(bookingRepo.findById(1L)).thenThrow(ValidationException.class);
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> bookingMapperService.addStatusToBooking(1L, 1L, null));
        validationException.getMessage();
    }

    @Test
    void testAddStatusToBooking_whenNotOwnerTryToApprove_thenThrowBookingNotFoundException() {
        when(bookingRepo.findById(1L)).thenReturn(Optional.ofNullable(newBooking));
        BookingNotFoundException bookingNotFoundException = assertThrows(BookingNotFoundException.class,
                () -> bookingMapperService.addStatusToBooking(2L, 1L, true));
        bookingNotFoundException.getMessage();
    }

    @Test
    void testBookingRequestPrepareForAdd_whenAvailableFalse_thenReturnValidationException() {
        ItemDto itemDto = ItemMapper.makeDtoFromItem(item).orElseThrow();
        itemDto.setAvailable(false);
        when(itemService.getItem(1L, bookerId2)).thenReturn(itemDto);
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> bookingMapperService.bookingRequestPrepareForAdd(bookerId2, newBookingRequestDto));
        validationException.getMessage();
    }

    @Test
    void testBookingRequestPrepareForAdd_whenStartDateNotValid_thenReturnValidationException() {
        ItemDto itemDto = ItemMapper.makeDtoFromItem(item).orElseThrow();
        when(itemService.getItem(1L, bookerId2)).thenReturn(itemDto);

        BookingRequestDto bookingRequestDtoStartAfterEnd = BookingRequestDto.builder()
                .itemId(ownerId1)
                .start(start)
                .end(start.minusDays(1))
                .build();

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> bookingMapperService.bookingRequestPrepareForAdd(bookerId2, bookingRequestDtoStartAfterEnd));
        validationException.getMessage();
    }

    @Test
    void testBookingRequestPrepareForAdd_whenStartDateIsInThePast_thenReturnValidationException() {
        ItemDto itemDto = ItemMapper.makeDtoFromItem(item).orElseThrow();
        when(itemService.getItem(1L, bookerId2)).thenReturn(itemDto);

        BookingRequestDto bookingRequestDtoStartAfterEnd = BookingRequestDto.builder()
                .itemId(ownerId1)
                .start(start.minusDays(5))
                .end(end)
                .build();

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> bookingMapperService.bookingRequestPrepareForAdd(bookerId2, bookingRequestDtoStartAfterEnd));
        validationException.getMessage();
    }

    @Test
    void testBookingRequestPrepareForAdd_whenStartEqualsEnd_thenReturnValidationException() {
        BookingRequestDto bookingRequestDtoWithStartAndEndEquals = BookingRequestDto.builder()
                .itemId(ownerId1)
                .start(start)
                .end(start)
                .build();

        ItemDto itemDto = ItemMapper.makeDtoFromItem(item).orElseThrow();
        when(itemService.getItem(1L, bookerId2)).thenReturn(itemDto);
        newBookingRequestDto.setStart(end);
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> bookingMapperService.bookingRequestPrepareForAdd(bookerId2, bookingRequestDtoWithStartAndEndEquals));
        validationException.getMessage();
    }

    @Test
    void testBookingRequestPrepareForAdd_whenRequestValid_thenReturnBooking() {
        ItemDto itemDto = ItemMapper.makeDtoFromItem(item).orElseThrow();

        BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                .itemId(ownerId1)
                .start(start)
                .end(end)
                .build();

        when(itemService.getItem(1L, bookerId2)).thenReturn(itemDto);
        when(userService.getUser(bookerId2)).thenReturn(UserMapper.makeDto(userBooker).get());
        when(userService.getUser(ownerId1)).thenReturn(UserMapper.makeDto(userOwner).get());

        assertEquals(newBooking, bookingMapperService.bookingRequestPrepareForAdd(bookerId2, bookingRequestDto));
    }

    @Test
    void testBookingRequestPrepareForAdd_whenUserEqualsOwner_thenThrowBookingNotFoundException() {
        ItemDto itemDto = ItemMapper.makeDtoFromItem(item).orElseThrow();

        BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                .itemId(ownerId1)
                .start(start)
                .end(end)
                .build();

        when(itemService.getItem(1L, ownerId1)).thenReturn(itemDto);
        when(userService.getUser(ownerId1)).thenReturn(UserMapper.makeDto(userOwner).get());
        when(userService.getUser(ownerId1)).thenReturn(UserMapper.makeDto(userOwner).get());

        BookingNotFoundException ex = assertThrows(BookingNotFoundException.class,
                () -> bookingMapperService.bookingRequestPrepareForAdd(ownerId1, bookingRequestDto));
        ex.getMessage();
    }

    @Test
    void testAccessVerification_whenUserIsNotBooker_thenThrowBookingNotFoundException() {
        BookingNotFoundException ex = assertThrows(BookingNotFoundException.class,
                () -> bookingMapperService.accessVerification(newBooking, 4L));
    }

    @Test
    void testPrepareResponseDtoList_whenResponseCorrectAndStateAll_thenReturnEmptyListDto() {
        List<Booking> listFromRepo = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 1);
        when(userService.getUser(bookerId2)).thenReturn(UserMapper.makeDto(userBooker).get());
        when(bookingRepo.findByBookerIdOrderByStartDesc(bookerId2, pageRequest)).thenReturn(listFromRepo);

        List<BookingResponseDto> expectedAnswerList = listFromRepo.stream()
                .map(booking -> BookingMapper.entityToResponseDto(booking).get())
                .collect(Collectors.toList());

        List<BookingResponseDto> actualAnswerList =
                bookingMapperService.prepareResponseDtoList(bookerId2, StateForBooking.ALL, 0, 1);

        assertEquals(expectedAnswerList, actualAnswerList);
    }

    @Test
    void testPrepareResponseDtoList_whenResponseCorrectBookingNotNullAndStateAll_thenReturnListBookingDto() {
        List<Booking> listFromRepo = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 1);
        when(userService.getUser(bookerId2)).thenReturn(UserMapper.makeDto(userBooker).get());
        when(bookingRepo.findByBookerIdOrderByStartDesc(bookerId2, pageRequest)).thenReturn(listFromRepo);

        List<BookingResponseDto> expectedAnswerList = listFromRepo.stream()
                .map(booking -> BookingMapper.entityToResponseDto(booking).get())
                .collect(Collectors.toList());

        List<BookingResponseDto> actualAnswerList =
                bookingMapperService.prepareResponseDtoList(bookerId2, StateForBooking.ALL, 0, 1);

        assertEquals(expectedAnswerList, actualAnswerList);
    }

    @Test
    void testPrepareResponseDtoList_whenResponseCorrectAndStateFuture_thenReturnEmptyListDto() {
        List<Booking> listFromRepo = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 1);

        when(userService.getUser(bookerId2)).thenReturn(UserMapper.makeDto(userBooker).get());
        when(bookingRepo.findAllByBookerIdAndStartAfterOrderByStartDesc(eq(bookerId2), Mockito.any(LocalDateTime.class),
                eq(pageRequest))).thenReturn(listFromRepo);

        List<BookingResponseDto> expectedAnswerList = listFromRepo.stream()
                .map(booking -> BookingMapper.entityToResponseDto(booking).get())
                .collect(Collectors.toList());
        List<BookingResponseDto> actualAnswerList =
                bookingMapperService.prepareResponseDtoList(bookerId2, StateForBooking.FUTURE, 0, 1);

        assertEquals(expectedAnswerList, actualAnswerList);
    }

    @Test
    void testPrepareResponseDtoList_whenResponseCorrectAndStateCurrent_thenReturnEmptyListDto() {
        List<Booking> listFromRepo = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 1);

        when(userService.getUser(bookerId2)).thenReturn(UserMapper.makeDto(userBooker).get());
        when(bookingRepo.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(bookerId2),
                Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                eq(pageRequest))).thenReturn(listFromRepo);

        List<BookingResponseDto> expectedAnswerList = listFromRepo.stream()
                .map(booking -> BookingMapper.entityToResponseDto(booking).get())
                .collect(Collectors.toList());
        List<BookingResponseDto> actualAnswerList =
                bookingMapperService.prepareResponseDtoList(bookerId2, StateForBooking.CURRENT, 0, 1);

        assertEquals(expectedAnswerList, actualAnswerList);
    }

    @Test
    void testPrepareResponseDtoList_whenResponseCorrectAndStatePast_thenReturnEmptyListDto() {
        List<Booking> listFromRepo = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 1);

        when(userService.getUser(bookerId2)).thenReturn(UserMapper.makeDto(userBooker).get());
        when(bookingRepo.findAllByBookerIdAndEndBeforeOrderByStartDesc(eq(bookerId2),
                Mockito.any(LocalDateTime.class), eq(pageRequest))).thenReturn(listFromRepo);

        List<BookingResponseDto> expectedAnswerList = listFromRepo.stream()
                .map(booking -> BookingMapper.entityToResponseDto(booking).get())
                .collect(Collectors.toList());
        List<BookingResponseDto> actualAnswerList =
                bookingMapperService.prepareResponseDtoList(bookerId2, StateForBooking.PAST, 0, 1);

        assertEquals(expectedAnswerList, actualAnswerList);
    }

    @Test
    void testPrepareResponseDtoList_whenResponseCorrectAndStateWaiting_thenReturnEmptyListDto() {
        List<Booking> listFromRepo = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 1);

        when(userService.getUser(bookerId2)).thenReturn(UserMapper.makeDto(userBooker).get());
        when(bookingRepo.findAllByBookerIdAndStatusOrderByStartDesc(bookerId2, pageRequest, StatusOfBooking.WAITING))
                .thenReturn(listFromRepo);

        List<BookingResponseDto> expectedAnswerList = listFromRepo.stream()
                .map(booking -> BookingMapper.entityToResponseDto(booking).get())
                .collect(Collectors.toList());
        List<BookingResponseDto> actualAnswerList =
                bookingMapperService.prepareResponseDtoList(bookerId2, StateForBooking.WAITING, 0, 1);

        assertEquals(expectedAnswerList, actualAnswerList);
    }

    @Test
    void testPrepareResponseDtoList_whenResponseCorrectAndStateRejected_thenReturnEmptyListDto() {
        List<Booking> listFromRepo = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 1);

        when(userService.getUser(bookerId2)).thenReturn(UserMapper.makeDto(userBooker).get());
        when(bookingRepo.findAllByBookerIdAndStatusOrderByStartDesc(bookerId2, pageRequest, StatusOfBooking.REJECTED))
                .thenReturn(listFromRepo);

        List<BookingResponseDto> expectedAnswerList = listFromRepo.stream()
                .map(booking -> BookingMapper.entityToResponseDto(booking).get())
                .collect(Collectors.toList());
        List<BookingResponseDto> actualAnswerList =
                bookingMapperService.prepareResponseDtoList(bookerId2, StateForBooking.REJECTED, 0, 1);

        assertEquals(expectedAnswerList, actualAnswerList);
    }

    @Test
    void testPrepareResponseDtoListForOwner_whenResponseCorrectBookingAndStateAll_thenReturnListWithDto() {
        ItemDto itemDto = ItemMapper.makeDtoFromItem(item).orElseThrow();
        when(userService.getUser(ownerId1)).thenReturn(UserMapper.makeDto(userOwner).orElseThrow());
        when(itemService.getItems(1L)).thenReturn(List.of(itemDto));

        List<Booking> listFromRepo = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 1);
        when(bookingRepo.findAllByItemOwnerIdOrderByStartDesc(ownerId1, pageRequest)).thenReturn(listFromRepo);

        List<BookingResponseDto> expectedAnswerList = listFromRepo.stream()
                .map(booking -> BookingMapper.entityToResponseDto(booking).get())
                .collect(Collectors.toList());

        List<BookingResponseDto> actualAnswerList =
                bookingMapperService.prepareResponseDtoListForOwner(ownerId1, StateForBooking.ALL, 0, 1);

        assertEquals(expectedAnswerList, actualAnswerList);
    }

    @Test
    void testPrepareResponseDtoListForOwner_whenResponseCorrectBookingAndStateWaiting_thenReturnListWithDto() {
        ItemDto itemDto = ItemMapper.makeDtoFromItem(item).orElseThrow();
        when(userService.getUser(ownerId1)).thenReturn(UserMapper.makeDto(userOwner).orElseThrow());
        when(itemService.getItems(1L)).thenReturn(List.of(itemDto));

        List<Booking> listFromRepo = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 1);
        when(bookingRepo.findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId1, pageRequest, StatusOfBooking.WAITING))
                .thenReturn(listFromRepo);

        List<BookingResponseDto> expectedAnswerList = listFromRepo.stream()
                .map(booking -> BookingMapper.entityToResponseDto(booking).get())
                .collect(Collectors.toList());

        List<BookingResponseDto> actualAnswerList =
                bookingMapperService.prepareResponseDtoListForOwner(ownerId1, StateForBooking.WAITING, 0, 1);

        assertEquals(expectedAnswerList, actualAnswerList);
    }

    @Test
    void testPrepareResponseDtoListForOwner_whenResponseCorrectBookingAndStateRejected_thenReturnListWithDto() {
        ItemDto itemDto = ItemMapper.makeDtoFromItem(item).orElseThrow();
        when(userService.getUser(ownerId1)).thenReturn(UserMapper.makeDto(userOwner).orElseThrow());
        when(itemService.getItems(1L)).thenReturn(List.of(itemDto));

        List<Booking> listFromRepo = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 1);
        when(bookingRepo.findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId1, pageRequest, StatusOfBooking.REJECTED))
                .thenReturn(listFromRepo);

        List<BookingResponseDto> expectedAnswerList = listFromRepo.stream()
                .map(booking -> BookingMapper.entityToResponseDto(booking).get())
                .collect(Collectors.toList());

        List<BookingResponseDto> actualAnswerList =
                bookingMapperService.prepareResponseDtoListForOwner(ownerId1, StateForBooking.REJECTED, 0, 1);

        assertEquals(expectedAnswerList, actualAnswerList);
    }

    @Test
    void testPrepareResponseDtoListForOwner_whenResponseCorrectBookingAndStateCurrent_thenReturnListWithDto() {
        ItemDto itemDto = ItemMapper.makeDtoFromItem(item).orElseThrow();
        when(userService.getUser(ownerId1)).thenReturn(UserMapper.makeDto(userOwner).orElseThrow());
        when(itemService.getItems(1L)).thenReturn(List.of(itemDto));

        List<Booking> listFromRepo = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 1);
        when(bookingRepo.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(eq(ownerId1),
                Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class), eq(pageRequest)))
                .thenReturn(listFromRepo);

        List<BookingResponseDto> expectedAnswerList = listFromRepo.stream()
                .map(booking -> BookingMapper.entityToResponseDto(booking).get())
                .collect(Collectors.toList());

        List<BookingResponseDto> actualAnswerList =
                bookingMapperService.prepareResponseDtoListForOwner(ownerId1, StateForBooking.CURRENT, 0, 1);

        assertEquals(expectedAnswerList, actualAnswerList);
    }

    @Test
    void testPrepareResponseDtoListForOwner_whenResponseCorrectBookingAndStatePast_thenReturnListWithDto() {
        ItemDto itemDto = ItemMapper.makeDtoFromItem(item).orElseThrow();
        when(userService.getUser(ownerId1)).thenReturn(UserMapper.makeDto(userOwner).orElseThrow());
        when(itemService.getItems(1L)).thenReturn(List.of(itemDto));

        List<Booking> listFromRepo = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 1);
        when(bookingRepo.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(ownerId1),
                Mockito.any(LocalDateTime.class), eq(pageRequest)))
                .thenReturn(listFromRepo);

        List<BookingResponseDto> expectedAnswerList = listFromRepo.stream()
                .map(booking -> BookingMapper.entityToResponseDto(booking).get())
                .collect(Collectors.toList());

        List<BookingResponseDto> actualAnswerList =
                bookingMapperService.prepareResponseDtoListForOwner(ownerId1, StateForBooking.PAST, 0, 1);

        assertEquals(expectedAnswerList, actualAnswerList);
    }

    @Test
    void testPrepareResponseDtoListForOwner_whenResponseCorrectBookingAndStateFuture_thenReturnListWithDto() {
        ItemDto itemDto = ItemMapper.makeDtoFromItem(item).orElseThrow();
        when(userService.getUser(ownerId1)).thenReturn(UserMapper.makeDto(userOwner).orElseThrow());
        when(itemService.getItems(1L)).thenReturn(List.of(itemDto));

        List<Booking> listFromRepo = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 1);
        when(bookingRepo.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(eq(ownerId1),
                Mockito.any(LocalDateTime.class), eq(pageRequest))).thenReturn(listFromRepo);

        List<BookingResponseDto> expectedAnswerList = listFromRepo.stream()
                .map(booking -> BookingMapper.entityToResponseDto(booking).get())
                .collect(Collectors.toList());

        List<BookingResponseDto> actualAnswerList =
                bookingMapperService.prepareResponseDtoListForOwner(ownerId1, StateForBooking.FUTURE, 0, 1);

        assertEquals(expectedAnswerList, actualAnswerList);
    }


    @Test
    void testPrepareResponseDtoListForOwner_whenRequestNotFromOwner_thenThrowUserNotFoundException() {
        when(userService.getUser(bookerId2)).thenReturn(UserMapper.makeDto(userBooker).orElseThrow());
        when(itemService.getItems(bookerId2)).thenReturn(new ArrayList<>());
        ItemNotFoundException itemNotFoundException = assertThrows(ItemNotFoundException.class,
                () -> bookingMapperService.prepareResponseDtoListForOwner(bookerId2, StateForBooking.ALL, 0, 1));
        itemNotFoundException.getMessage();
    }
}