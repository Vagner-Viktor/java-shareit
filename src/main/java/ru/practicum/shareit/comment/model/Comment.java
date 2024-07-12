package ru.practicum.shareit.comment.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
public class Comment {
    @NotNull
    private Long id;
    @NotBlank
    private String text;
    @NotNull
    private Item item;
    @NotNull
    private User author;
    @NotNull
    private LocalDateTime created;
}
