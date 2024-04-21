package ru.practicum.shareit.user.model;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * TODO Sprint add-controllers.
 */
@Component
@Data
public class User {
    private Integer id;
    private String name;
    private String email;
}
