package ru.practicum.shareit.user.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class UserDto {
    private Integer id;
    private String name;
    private String email;
}
