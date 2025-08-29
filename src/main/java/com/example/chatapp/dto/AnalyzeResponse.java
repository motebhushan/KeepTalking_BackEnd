package com.example.chatapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyzeResponse {
    private List<String> mistakes;
    private List<String> suggestions;
    private List<String> vocabTips;
}