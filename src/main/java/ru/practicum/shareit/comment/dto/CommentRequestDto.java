package ru.practicum.shareit.comment.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
public class CommentRequestDto {
    private Long authorId;
    private Long itemId;

    @NotBlank(message = "Комментарий нельзя создать без описания")
    private String text;

    public CommentRequestDto(Long authorId, Long itemId, String text) {
        this.authorId = authorId;
        this.itemId = itemId;
        this.text = text;
    }
}
