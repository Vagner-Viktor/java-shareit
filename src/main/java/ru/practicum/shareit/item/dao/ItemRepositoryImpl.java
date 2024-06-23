package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

@RequiredArgsConstructor
@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Collection<Item> items;

    @Override
    public Collection<Item> findAll() {
        return items;
    }

    @Override
    public Item create(Item item) {
        items.add(item);
        return item;
    }

    @Override
    public Item update(Item item) {
        return null;
    }
}
