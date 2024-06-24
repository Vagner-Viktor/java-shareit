package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

@RequiredArgsConstructor
@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Collection<Item> items;
    private Long index = 0L;

    @Override
    public Collection<Item> findAll() {
        return items;
    }

    @Override
    public Item create(Item item) {
        item.setId(++index);
        items.add(item);
        return item;
    }

    @Override
    public Item update(Item newItem) {
        Item item = findItemById(newItem.getId());
        if (newItem.getName() != null
                && !newItem.getName().isBlank()) {
            item.setName(newItem.getName());
        }
        if (newItem.getDescription() != null
                && !newItem.getDescription().isBlank()) {
            item.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            item.setAvailable(newItem.getAvailable());
        }
        return item;
    }

    @Override
    public Item findItemById(Long itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Item (id = " + itemId + ") not found!"));
    }

    @Override
    public Collection<Item> findItemsByUserId(Long userId) {
        return items.stream()
                .filter(item -> item.getOwner().equals(userId))
                .toList();
    }

    @Override
    public boolean isItemExist(Long itemId) {
        return items.stream()
                .anyMatch(item -> item.getId().equals(itemId));
    }

    @Override
    public void delete(Long itemId) {
        items.remove(findItemById(itemId));
    }

    @Override
    public boolean isOwner(Long userId, Long itemId) {
        return findItemById(itemId).getOwner().equals(userId);
    }

    @Override
    public Collection<Item> findItemsByText(String text) {
        return items.stream()
                .filter(item -> (item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                        && item.getAvailable().equals(true))
                .toList();
    }
}
