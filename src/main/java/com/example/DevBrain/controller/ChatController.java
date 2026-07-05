package com.example.DevBrain.controller;

import com.example.DevBrain.dto.ChatRequest;
import com.example.DevBrain.dto.ChatResponse;
import com.example.DevBrain.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*") // Allows React UI to communicate
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askQuestion(
            @Valid @RequestBody ChatRequest request,
            jakarta.servlet.http.HttpServletRequest servletRequest) {
        
        String userId = (String) servletRequest.getAttribute("userId");
        String scopedDatasetName = (userId != null) ? userId + "_" + request.getDatasetName() : request.getDatasetName();
        
        ChatResponse response = chatService.askQuestion(
                request.getQuestion(),
                scopedDatasetName
        );
        return ResponseEntity.ok(response);
    }
}
