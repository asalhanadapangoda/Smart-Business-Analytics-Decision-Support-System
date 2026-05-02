package com.sbadss.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    private String message;

    private String sessionId;
    private Long branchId;
}
