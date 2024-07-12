package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

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
    public ItemDto findItemById(Long itemId) {
        return ItemMapper.toItemDto(itemRepository.findById(itemId).orElseThrow(() -> {
            throw new NotFoundException("Item (id = " + itemId + ") not found!");
        }));
    }

    @Override
    public Collection<ItemDto> findItemsByUserId(Long userId) {
        return ItemMapper.toItemsDtoCollection(itemRepository.findAllByOwnerId(userId));
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
//        if (!userRepository.isUserExist(userId)) {
//            throw new NotFoundException("User (id = " + userId + ") not found!");
//        }
        if (itemId != null && !(itemRepository.findById(itemId).orElse(null).getOwner().getId()==userId)) {
            throw new NotFoundException("Only the owner can edit an item!");
        }
        if (itemId != null && !itemRepository.existsById(itemId)) {
            throw new NotFoundException("Item (id = " + itemId + ") not found!");
        }
    }
}
