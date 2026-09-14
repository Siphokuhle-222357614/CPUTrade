package za.ac.cput.cputrade.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageEditRequest {

    @NotBlank
    @Size(max = 2000)
    private String body;
}
