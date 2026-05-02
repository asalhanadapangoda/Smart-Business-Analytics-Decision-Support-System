package com.sbadss.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private String sessionId;
    private String message;
    private String intent;
    private Double confidence;
    private List<String> suggestedPrompts;
    private Object data;
}
