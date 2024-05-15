package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import java.time.LocalDateTime;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Integer id;

    private String text;

    private Item item;

    private String authorName;

    private LocalDateTime created;
}
