package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "authorName", source = "comment.author.name")
    @Mapping(target = "itemId", source = "comment.item.id")
    CommentDto toCommentDto(Comment comment);

    List<CommentDto> toCommentDto(List<Comment> comments);

}