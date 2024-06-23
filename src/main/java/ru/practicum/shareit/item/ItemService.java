package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    Collection<ItemDto> findAll();

    ItemDto create(ItemDto itemDto);

    ItemDto update(ItemDto itemDto);
}
