package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ItemRequestDto {
    @NotNull
    private Long id;
    private String description;
    @NotNull
    private UserDto requestor;
    @NotNull
    private LocalDateTime created;
}
