package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    @Override
    public Collection<ItemDto> findAll() {
        return List.of();
    }

    @Override
    public ItemDto create(ItemDto itemDto) {
        return null;
    }

    @Override
    public ItemDto update(ItemDto itemDto) {
        return null;
    }
}
