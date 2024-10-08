package com.example.CodeGeneration.controller;

import com.example.CodeGeneration.model.CodeSnippetEntity;
import com.example.CodeGeneration.service.CodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CodeController {

    @Autowired
    private CodeService codeService;

    @PostMapping("/codesnippet")
    public ResponseEntity<CodeSnippetEntity> createCodeSnippet(@RequestBody CodeSnippetEntity codeSnippet) {
        CodeSnippetEntity savedCodeSnippet = codeService.saveCodeSnippet(codeSnippet);
        return ResponseEntity.ok(savedCodeSnippet);
    }
}
