package com.example.DevBrain.service;

import com.example.DevBrain.dto.ChatResponse;

public interface ChatService {
    ChatResponse askQuestion(String question, String datasetName);
}
