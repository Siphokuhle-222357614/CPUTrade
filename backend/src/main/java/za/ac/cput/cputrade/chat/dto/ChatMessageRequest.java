package za.ac.cput.cputrade.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import za.ac.cput.cputrade.chat.LocationSuggestion;

@Getter
@Setter
public class ChatMessageRequest {

    @NotBlank
    @Size(max = 2000)
    private String body;

    /** Optional — set to suggest a safe on-campus meetup spot (US4.2). */
    private LocationSuggestion locationSuggestion;
}
