package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.dao.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentRequestDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public Collection<ItemDto> findAll() {
        return ItemMapper.toItemsDtoCollection(itemRepository.findAll());
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        validation(userId, null);
        return ItemMapper.toItemDto(itemRepository.save(
                Item.builder()
                        .name(itemDto.getName())
                        .owner(userRepository.findById(userId).orElseThrow(() -> {
                            throw new NotFoundException("User id = " + userId + " not found!");
                        }))
                        .description(itemDto.getDescription())
                        .available(itemDto.getAvailable())
                        .request(null)
                        .build()
        ));
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        validation(userId, itemId);

        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            throw new NotFoundException("Item id = " + itemId + " not found!");
        });
        if (itemDto.getName() != null
                && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null
                && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        if (commentRequestDto == null || commentRequestDto.getText().isEmpty() || commentRequestDto.getText().isBlank()) {
            throw new ValidationException("Comment is empty!");
        }
        if (bookingRepository.findAllByBookerIdAndItemIdAndStatusAndEndBefore(userId, itemId, "APPROVED", LocalDateTime.now()).isEmpty()) {
            throw new ValidationException("The user (id = " + userId + ") did not book this item (id = " + itemId + ") for rent");
        }
        return CommentMapper.toCommentDto(commentRepository.save(Comment.builder()
                .author(userRepository.findById(userId).orElseThrow(() -> {
                    throw new NotFoundException("User id = " + userId + " not found!");
                }))
                .item(itemRepository.findById(itemId).orElseThrow(() -> {
                    throw new NotFoundException("Item id = " + itemId + " not found!");
                }))
                .text(commentRequestDto.getText())
                .created(LocalDateTime.now())
                .build()));
    }

    @Override
    public ItemInfoDto findItemById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            throw new NotFoundException("Item (id = " + itemId + ") not found!");
        });
        return ItemMapper.toItemInfoDto(
                item,
                BookingMapper.toBookingDateInfoDto(bookingRepository.findFirstByItemIdAndItemOwnerIdAndStartBeforeAndStatusOrderByStartDesc(itemId, userId, LocalDateTime.now(), "APPROVED").orElse(null)),
                BookingMapper.toBookingDateInfoDto(bookingRepository.findFirstByItemIdAndItemOwnerIdAndStartAfterAndStatusOrderByStartAsc(itemId, userId, LocalDateTime.now(), "APPROVED").orElse(null)),
                CommentMapper.toCommentsDtoCollection(commentRepository.findAllByItemId(itemId))
        );
    }

    @Override
    public Collection<ItemInfoDto> findItemsByUserId(Long userId) {
        HashMap<Long, Item> items = new HashMap<>();
        itemRepository.findAllByOwnerIdOrderByIdAsc(userId).stream()
                .forEach(item -> items.put(item.getId(), item));
        HashMap<Long, Booking> bookingsBefore = new HashMap<>();
        bookingRepository.findAllByItemIdInAndItemOwnerIdAndStartBeforeAndStatusOrderByStartDesc(items.keySet(), userId, LocalDateTime.now(), "APPROVED")
                .stream().forEach(booking -> bookingsBefore.put(booking.getItem().getId(), booking));
        HashMap<Long, Booking> bookingsAfter = new HashMap<>();
        bookingRepository.findAllByItemIdInAndItemOwnerIdAndEndAfterAndStatusOrderByStartDesc(items.keySet(), userId, LocalDateTime.now(), "APPROVED")
                .stream().forEach(booking -> bookingsAfter.put(booking.getItem().getId(), booking));
        return items.values().stream()
                .map(item -> ItemMapper.toItemInfoDto(item,
                        BookingMapper.toBookingDateInfoDto(bookingsBefore.get(item.getId())),
                        BookingMapper.toBookingDateInfoDto(bookingsAfter.get(item.getId())),
                        CommentMapper.toCommentsDtoCollection(commentRepository.findAllByItemId(item.getId()))))
                .toList();
    }

    @Override
    public void delete(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    public Collection<ItemDto> findItemsByText(String text) {
        if (text.isBlank() || text.isEmpty()) return List.of();
        return ItemMapper.toItemsDtoCollection(itemRepository.findByNameOrDescriptionContaining(text));
    }

    private void validation(Long userId, Long itemId) {
        if (userId == null) {
            throw new ValidationException("Owner id not specified!");
        }
        if (itemId != null && !(Objects.equals(Objects.requireNonNull(itemRepository.findById(itemId).orElse(null)).getOwner().getId(), userId))) {
            throw new NotFoundException("Only the owner can edit an item!");
        }
        if (itemId != null && !itemRepository.existsById(itemId)) {
            throw new NotFoundException("Item (id = " + itemId + ") not found!");
        }
    }
}
