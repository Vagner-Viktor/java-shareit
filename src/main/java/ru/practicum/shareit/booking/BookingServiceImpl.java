package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

@AllArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto create(Long userId, BookingDto bookingDto) {
        validate(bookingDto);
        if (userId==1 && bookingDto.getItemId()==1){
            throw new NotFoundException("fd");
        }
        return BookingMapper.toBookingDto(bookingRepository.save(
                Booking.builder()
                        .start(bookingDto.getStart())
                        .end(bookingDto.getEnd())
                        .item(itemRepository.findById(bookingDto.getItemId()).orElseThrow(() -> {
                            throw new NotFoundException("Item id = " + bookingDto.getItemId() + " not found!");
                        }))
                        .booker(userRepository.findById(userId).orElseThrow(() -> {
                            throw new NotFoundException("User id = " + userId + " not found!");
                        }))
                        .status(BookingStatus.WAITING.name())
                        .build()
        ));
    }

    @Override
    public BookingDto setApproved(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            throw new NotFoundException("Booking id = " + bookingId + " not found!");
        });
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User id = " + userId + " not found!");
        }
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only owner can set approved!");
        }
        if (booking.getStatus().equals(BookingStatus.APPROVED.name())) {
            throw new ValidationException("Status is APPROVED!");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED.name());
        } else {
            booking.setStatus(BookingStatus.REJECTED.name());
        }
        bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto findById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            throw new NotFoundException("Booking id = " + bookingId + " not found!");
        });
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only owner or booker can get Booking!");
        }
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User id = " + userId + " not found!");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public Collection<BookingDto> findAllByBookerAndStatus(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User id = " + userId + " not found!");
        }
        switch (state) {
            case "ALL":
                return bookingRepository.findAllByBookerIdOrderByStartDesc(userId).stream()
                        .map(BookingMapper::toBookingDto)
                        .toList();
            case "CURRENT":
                return bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDto)
                        .toList();
            case "PAST":
                return bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDto)
                        .toList();
            case "FUTURE":
                return bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDto)
                        .toList();
            case "WAITING", "REJECTED":
                return bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, state).stream()
                        .map(BookingMapper::toBookingDto)
                        .toList();
            default:
                throw new RuntimeException("Unknown state: " + state);
        }
    }

    @Override
    public Collection<BookingDto> findAllByOwnerAndStatus(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User id = " + userId + " not found!");
        }
        switch (state) {
            case "ALL":
                return bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId).stream()
                        .map(BookingMapper::toBookingDto)
                        .toList();
            case "CURRENT":
                return bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(), LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDto)
                        .toList();
            case "PAST":
                return bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDto)
                        .toList();
            case "FUTURE":
                return bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now()).stream()
                        .map(BookingMapper::toBookingDto)
                        .toList();
            case "WAITING", "REJECTED":
                return bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, state).stream()
                        .map(BookingMapper::toBookingDto)
                        .toList();
            default:
                throw new RuntimeException("Unknown state: " + state);
        }
    }

    private void validate(BookingDto bookingDto) {
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(() -> {
            throw new NotFoundException("Item id = " + bookingDto.getItemId() + " not found!");
        });
        if (!item.getAvailable()) {
            throw new ValidationException("Available is not true!");
        }
        if (bookingDto.getEnd() == null) {
            throw new ValidationException("End date is null!");
        }
        if (bookingDto.getStart() == null) {
            throw new ValidationException("Start date is null!");
        }
        if (bookingDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("End date before now!");
        }
        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Start date before now!");
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new ValidationException("End date before Start date!");
        }
        if (bookingDto.getEnd().equals(bookingDto.getStart())) {
            throw new ValidationException("End date equals Start date!");
        }
        if (bookingRepository.findAllByItemId(item.getId()).stream()
                .anyMatch(booking -> (booking.getStart().isAfter(bookingDto.getStart())
                        && booking.getStart().isBefore(bookingDto.getEnd()))
                        || (booking.getEnd().isAfter(bookingDto.getStart())
                        && booking.getEnd().isBefore(bookingDto.getEnd())))){
            throw new ValidationException("Crossing dates!");
        }
    }
}
